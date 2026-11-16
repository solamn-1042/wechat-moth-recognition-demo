const { BASE_URL } = require('../../utils/config');
// pages/history/history.js
Page({
    data: {
      serverBaseUrl: BASE_URL,
      records: [],
      page: 1,
      size: 10,
      total: 0,
      hasMore: false,
      userId: null,
      isLoggedIn: false  // 新增登录状态
    },
  
    onLoad: function(options) {
      this.checkLoginStatus();
    },
    
    onShow: function() {
      // 每次进入页面检查登录状态
      this.checkLoginStatus();
    },

    checkLoginStatus: function() {
        // 从 userInfo 获取用户ID，与 logs.js 保持一致
        const userInfo = wx.getStorageSync('userInfo');
        console.log('从本地存储获取的用户信息:', userInfo);
        
        const userId = userInfo ? userInfo.id : null;
        console.log("提取的userId:", userId);
      
        this.setData({ 
          isLoggedIn: !!userId,
          userId: userId || null
        });
        
        console.log("登录状态:", !!userId);
      
        if (userId) {
          this.loadHistoryRecords();
        } else {
          console.log("未检测到登录信息，显示登录提示");
        }
      },
    
    onPullDownRefresh: function() {
      if (this.data.isLoggedIn) {
        // 重置页码并刷新数据
        this.setData({
          page: 1,
          records: []
        });
        this.loadHistoryRecords();
      } else {
        wx.stopPullDownRefresh();
      }
    },
    
    goToLogin: function() {
      // 假设登录页面的路径为 /pages/login/login
      wx.navigateTo({
        url: '/pages/login/login'
      });
      
      // 临时测试：直接设置一个用户ID
      // wx.setStorageSync('userId', 1);
      // this.checkLoginStatus();
    },
    
    loadHistoryRecords: function() {
      const { userId, page, size } = this.data;
      
      if (!userId) {
        return;  // 如果没有用户ID，不加载数据
      }
      
      wx.showLoading({ title: '加载中...' });
      
      wx.request({
        url: `${this.data.serverBaseUrl}/user/history`,
        method: 'GET',
        data: {
          userId,
          page,
          size
        },
        success: (res) => {
          if (res.statusCode === 200 && res.data) {
            const newRecords = page === 1 ? res.data.records : [...this.data.records, ...res.data.records];
            
            this.setData({
              records: newRecords,
              total: res.data.total,
              hasMore: newRecords.length < res.data.total
            });
          } else {
            wx.showToast({
              title: '加载失败',
              icon: 'none'
            });
          }
        },
        fail: (err) => {
          console.error('加载历史记录失败:', err);
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          });
        },
        complete: () => {
          wx.hideLoading();
          wx.stopPullDownRefresh();
        }
      });
    },
    
    loadMore: function() {
      if (this.data.hasMore) {
        this.setData({
          page: this.data.page + 1
        });
        this.loadHistoryRecords();
      }
    },
    
    viewDetail: function(e) {
      const record = e.currentTarget.dataset.record;
      
      // 将记录信息传递到详情页
      wx.navigateTo({
        url: `/pages/detail/detail?id=${record.id}`
      });
    },
    
    goToUpload: function() {
      // 修改为 navigateTo，因为这可能不是一个 tabBar 页面
      wx.navigateTo({
        url: '/pages/upload/upload'
      });
    },
    
    formatTime: function(timeStr) {
      if (!timeStr) return '';
      
      const date = new Date(timeStr);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hour = date.getHours().toString().padStart(2, '0');
      const minute = date.getMinutes().toString().padStart(2, '0');
      
      return `${year}-${month}-${day} ${hour}:${minute}`;
    }
  });