// app.js
App({
    onLaunch() {
      // 展示本地存储能力
      const logs = wx.getStorageSync('logs') || []
      logs.unshift(Date.now())
      wx.setStorageSync('logs', logs)
  
      // 尝试从本地存储恢复用户ID
      const userId = wx.getStorageSync('userId')
      if (userId) {
        this.globalData.userId = userId;
        this.globalData.isLoggedIn = true;
        console.log('从本地存储恢复用户ID:', userId)
      }
  
      // 登录
      wx.login({
        success: res => {
          // 发送 res.code 到后台换取 openId, sessionKey, unionId
          console.log('微信登录成功:', res.code)
          // 您可以在这里调用自己的后端接口，获取用户信息
        }
      })
    },
    
    // 添加用户登录方法
    userLogin: function(userId) {
      // 存储用户ID到全局变量
      this.globalData.userId = userId
      // 同时存储到本地存储，确保应用重启后仍能恢复登录状态
      wx.setStorageSync('userId', userId)
      console.log('用户登录成功，用户ID:', userId)
    },
    
    // 添加用户注销方法
    userLogout: function() {
      this.globalData.userId = null
      this.globalData.userInfo = null
      wx.removeStorageSync('userId')
      wx.removeStorageSync('userInfo')
      console.log('用户已注销')
    },
    
    // 检查用户是否已登录
    isUserLoggedIn: function() {
      return !!this.globalData.userId
    },
    
    globalData: {
      userInfo: null,
      userId: null  // 添加userId字段
    },
    
    networkTimeout: {
      "request": 1000000,
      "connectSocket": 100000,
      "uploadFile": 100000,
      "downloadFile": 100000
    },
  })