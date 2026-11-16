const { BASE_URL } = require('../../utils/config');
// index.js
// 获取应用实例
Page({
    data: {
      hasUserInfo: false,
      userInfo: null
    },
  
    onShow() {
      const app = getApp();
      if (app.globalData && app.globalData.userInfo) {
        this.setData({
          hasUserInfo: true,
          userInfo: app.globalData.userInfo
        });
      } else {
        const userInfo = wx.getStorageSync('userInfo');
        if (userInfo) {
          this.setData({
            hasUserInfo: true,
            userInfo: userInfo
          });
        } else {
          this.setData({
            hasUserInfo: false,
            userInfo: null
          });
        }
      }
    },  
    // 事件处理函数
    test(){
        let that = this
        wx.request({
          url: BASE_URL + '/user/hello',
          success(res){
              console.log("success",res.data)
              that.setData({
                  dataList:res.data
              })
          },
          fail(res){
              console.log("fail",res)
          }
        })
    },
   
    clickme:function(e)//动作函数，完成点击动作
    {
      wx.navigateTo({
        url: '/pages/upload/upload',
  
      })//点击后到达上传页面
    },
  
    goToLogin: function(e) {
      wx.navigateTo({
        url: '/pages/login/login',
      }); // 点击后跳转到登录页面
    },

    mothInfo() {
        wx.navigateTo({
          url: '/pages/moth-info/moth-info'
        });
      },

    history(){
        wx.navigateTo({
            url: '/pages/history/history'
          });
    }
  });