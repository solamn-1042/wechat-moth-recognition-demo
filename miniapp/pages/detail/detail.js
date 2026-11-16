const { BASE_URL } = require('../../utils/config');
Page({
    data: {
      serverBaseUrl: BASE_URL,
      recordId: null,
      record: null
    },
  
    onLoad: function(options) {
      if (options.id) {
        this.setData({
          recordId: options.id
        });
        this.loadRecordDetail();
      } else {
        wx.showToast({
          title: '参数错误',
          icon: 'none'
        });
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      }
    },
  
    loadRecordDetail: function() {
      wx.showLoading({ title: '加载中...' });
      wx.request({
        url: `${this.data.serverBaseUrl}/user/history/image`,
        method: 'GET',
        data: {
          recordId: this.data.recordId
        },
        success: (res) => {
            if (res.statusCode === 200 && res.data && res.data.id) {
              const record = res.data;
              record.formattedTime = this.formatTime(record.createTime);
              this.setData({
                record
              });
            } else {
              wx.showToast({
                title: '记录不存在',
                icon: 'none'
              });
              setTimeout(() => {
                wx.navigateBack();
              }, 1500);
            }
          },
        fail: (err) => {
          console.error('获取记录详情失败:', err);
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          });
        },
        complete: () => {
          wx.hideLoading();
        }
      });
    },
  
    goBack: function() {
      wx.navigateBack();
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