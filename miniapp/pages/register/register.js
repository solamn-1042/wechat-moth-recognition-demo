const { BASE_URL } = require('../../utils/config');
// register.js
Page({
    data: {
      username: '',
      password: '',
      confirmPassword: '',
      nickname: ''
    },
    
    onLoad: function() {
      // 页面加载时的逻辑
    },
    
    inputUsername: function(e) {
      this.setData({
        username: e.detail.value
      });
    },
    
    inputPassword: function(e) {
      this.setData({
        password: e.detail.value
      });
    },
    
    inputConfirmPassword: function(e) {
      this.setData({
        confirmPassword: e.detail.value
      });
    },
    
    inputNickname: function(e) {
      this.setData({
        nickname: e.detail.value
      });
    },
    
    register: function() {
      const { username, password, confirmPassword, nickname } = this.data;
      
      // 表单验证
      if (!username || !password || !confirmPassword) {
        wx.showToast({
          title: '请填写完整注册信息',
          icon: 'none'
        });
        return;
      }
      
      if (password !== confirmPassword) {
        wx.showToast({
          title: '两次密码输入不一致',
          icon: 'none'
        });
        return;
      }
      
      if (username.length < 3) {
        wx.showToast({
          title: '用户名至少需要3个字符',
          icon: 'none'
        });
        return;
      }
      
      if (password.length < 6) {
        wx.showToast({
          title: '密码至少需要6个字符',
          icon: 'none'
        });
        return;
      }
      
      // 显示加载中
      wx.showLoading({
        title: '注册中...',
      });
      
      // 调用后端注册API
      wx.request({
        url: BASE_URL + '/user/register',
        method: 'POST',
        header: {
          'content-type': 'application/json'
        },
        data: {
          username,
          password,
          nickname: nickname || username, // 如果没有输入昵称，使用用户名作为昵称
          avatarUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0' // 默认头像
        },
        success: (res) => {
          wx.hideLoading();
          
          console.log('注册响应:', res.data);
          
          if (res.statusCode === 200 && res.data.code === 200) {
            // 注册成功
            wx.showToast({
              title: '注册成功',
              icon: 'success',
              duration: 1500,
              success: () => {
                // 延迟返回登录页
                setTimeout(() => {
                  wx.navigateBack({
                    delta: 1
                  });
                }, 1500);
              }
            });
          } else {
            // 注册失败
            let errorMsg = '注册失败，请重试';
            if (res.data && res.data.message) {
              errorMsg = res.data.message;
            }
            
            wx.showToast({
              title: errorMsg,
              icon: 'none'
            });
          }
        },
        fail: (err) => {
          wx.hideLoading();
          console.error('注册请求失败:', err);
          
          wx.showToast({
            title: '网络错误，请检查服务器连接',
            icon: 'none'
          });
        }
      });
    },
    
    goToLogin: function() {
      wx.navigateBack();
    }
  });