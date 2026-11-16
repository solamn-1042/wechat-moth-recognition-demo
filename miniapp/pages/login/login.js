const { BASE_URL } = require('../../utils/config');
// login.js
Page({
    data: {
      username: '',
      password: ''
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
    
    login: function() {
      const { username, password } = this.data;
      
      // 表单验证
      if (!username || !password) {
        wx.showToast({
          title: '请填写用户名和密码',
          icon: 'none'
        });
        return;
      }
      
      // 显示加载
      wx.showLoading({
        title: '登录中...',
      });
      
      // 调用后端登录API
      wx.request({
        url: BASE_URL + '/user/login',
        method: 'POST',
        header: {
          'content-type': 'application/json'
        },
        data: {
          username,
          password
        },
        success: (res) => {
          wx.hideLoading();
          
          console.log('登录响应:', res.data);
          
          if (res.statusCode === 200 && res.data.code === 200) {
            // 登录成功，保存用户信息和token
            const userData = res.data.data;
            
            // 保存用户信息到本地存储
            wx.setStorageSync('userInfo', userData);
            wx.setStorageSync('token', userData.token);
            
            // 提示登录成功
            wx.showToast({
              title: '登录成功',
              icon: 'success',
              duration: 1500,
              success: () => {
                // 返回主页
                setTimeout(() => {
                  wx.switchTab({
                    url: '/pages/index/index'
                  });
                }, 1500);
              }
            });
          } else {
            // 登录失败
            let errorMsg = '登录失败';
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
          console.error('登录请求失败:', err);
          
          wx.showToast({
            title: '网络错误，请检查服务器连接',
            icon: 'none'
          });
        }
      });
    },
    
    goToRegister: function() {
      wx.navigateTo({
        url: '/pages/register/register'
      });
    },
    
    goBack: function() {
      wx.navigateBack({
        delta: 1
      });
    }
  });