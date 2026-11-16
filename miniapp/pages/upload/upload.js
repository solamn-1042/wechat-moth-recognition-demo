const { BASE_URL } = require('../../utils/config');
// pages/upload/upload.js
Page({
    /**
     * 页面的初始数据
     */
    data: {
      imageList: "/pages/data/add.jpg",
      detections: [], // 存储所有检测结果
      mainDetection: null, // 存储主要检测结果
      isLoading: false, // 加载状态
      showResult: false // 是否显示结果
    },
  
    // 生命周期函数保持不变...
  
    uploadImage: function() {
      var that = this;
      wx.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'], // 注意：这里有个typo 'compreesed'
        sourceType: ['album', 'camera'],
        success: function(res) {
          that.setData({
            imageList: res.tempFilePaths[0],
            showResult: false, // 新上传图片时，隐藏之前的结果
            detections: [],
            mainDetection: null
          });
        }
      });
    },
  
    request: function() {
      var that = this;
      
      // 显示加载中
      this.setData({
        isLoading: true
      });
      
      wx.showLoading({
        title: '识别中...',
      });

      
  // 从缓存中获取用户信息
    const userInfo = wx.getStorageSync('userInfo');
    const userId = userInfo ? userInfo.id : null;
    console.log("正在上传图片，用户ID:", userId);
      
      wx.uploadFile({
        url: BASE_URL + '/user/request',
        filePath: that.data.imageList,
        name: 'img',
        formData: {
            userId: userId
        },
        success(res) {
          console.log("上传成功：", res);
          try {
            // 解析返回的JSON字符串
            const data = JSON.parse(res.data);
            console.log("解析后的数据:", data);
            
            // 从后端返回的结果中获取识别数据
            if (data.detections && data.detections.length > 0) {
              console.log("检测到结果:", data.detections);
              
              // 处理置信度百分比
              let mainDetection = data.main_detection;
              if (!mainDetection) {
                // 如果没有主检测结果，使用第一个结果
                mainDetection = data.detections[0];
                console.log("没有主检测结果，使用第一个检测结果:", mainDetection);
              }
              
              // 为主检测结果添加百分比和宽度类
              mainDetection.confidencePercent = (mainDetection.confidence * 100).toFixed(1) + "%";
              mainDetection.widthClass = "width-" + Math.round(mainDetection.confidence * 100 / 10) * 10;
              
              // 为所有检测结果添加百分比和宽度类
              let detections = data.detections;
              detections.forEach(item => {
                item.confidencePercent = (item.confidence * 100).toFixed(1) + "%";
                item.widthClass = "width-" + Math.round(item.confidence * 100 / 10) * 10;
              });
              
              that.setData({
                detections: detections,
                mainDetection: mainDetection,
                showResult: true
              });
            } else {
              console.log("返回数据中没有检测结果:", data);
              wx.showToast({
                title: '未能识别出飞蛾种类',
                icon: 'none'
              });
            }
            
            // 无论是否有检测结果，都下载并显示结果图片
            wx.downloadFile({
              url: BASE_URL + data.result_image_path,
              success(downloadRes) {
                console.log("下载成功：", downloadRes);
                if (downloadRes.statusCode == 200) {
                  console.log("设置图片路径:", downloadRes.tempFilePath);
                  that.setData({
                    imageList: downloadRes.tempFilePath,
                    isLoading: false
                  });
                } else {
                  console.log("下载状态码异常:", downloadRes.statusCode);
                  that.setData({ isLoading: false });
                }
              },
              fail(downloadErr) {
                console.log("下载失败：", downloadErr);
                wx.showToast({
                  title: '下载结果图片失败',
                  icon: 'none'
                });
                that.setData({ isLoading: false });
              },
              complete() {
                wx.hideLoading();
              }
            });
            
          } catch (e) {
            console.error("解析响应数据失败：", e);
            
            that.setData({ isLoading: false });
            wx.hideLoading();
            wx.showToast({title: '识别响应异常，请重试', icon: 'none'});
          }
        },
        fail(res) {
          console.log("上传失败：", res);
          wx.showToast({
            title: '上传图片失败',
            icon: 'none'
          });
          that.setData({ isLoading: false });
          wx.hideLoading();
        }
      });
    }
  });