const { BASE_URL } = require('../../utils/config');
// logs.js
Page({
    data: {
      userInfo: null,
      showEditProfileModal: false,
      showPasswordModal: false,
      editNickname: '',
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    },
  
    onLoad: function() {
      // 页面加载时检查是否已登录
      this.checkLoginStatus();
    },
    
    onShow: function() {
      // 每次显示页面时检查登录状态，确保数据最新
      this.checkLoginStatus();
    },
    
    // 检查登录状态并获取用户信息
    // checkLoginStatus: function() {
    //     const userInfo = wx.getStorageSync('userInfo');
    //     console.log('从本地存储获取的用户信息:', userInfo);
    //     console.log('头像URL:', userInfo ? userInfo.avatarUrl : 'null');
    //   if (userInfo) {
    //     this.setData({ 
    //       userInfo: userInfo,
    //       editNickname: userInfo.nickname || userInfo.username
    //     });
    //   }
    // },
    checkLoginStatus: function() {
        const userInfo = wx.getStorageSync('userInfo');
        const token = wx.getStorageSync('token');
        if (!userInfo || !token) {
          // 未登录，跳转到登录页
          wx.navigateTo({
            url: '/pages/login/login'
          });
          return;
        }
        this.setData({ 
          userInfo: userInfo,
          editNickname: userInfo.nickname || userInfo.username
        });
    },
    
    // 跳转到登录页面
    goToLogin: function() {
      wx.navigateTo({
        url: '/pages/login/login'
      });
    },
    
    // 退出登录
    logout: function() {
      wx.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            // 清除本地存储的用户信息和token
            wx.removeStorageSync('userInfo');
            wx.removeStorageSync('token');
            
            // 更新页面状态
            this.setData({
              userInfo: null
            });
            
            wx.showToast({
              title: '已退出登录',
              icon: 'success'
            });
          }
        }
      });
    },
    
    // 显示编辑个人资料弹窗
    showEditProfile: function() {
      this.setData({
        showEditProfileModal: true
      });
    },
    
    // 显示修改密码弹窗
    showChangePassword: function() {
      this.setData({
        showPasswordModal: true,
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      });
    },
    
    // 隐藏所有弹窗
    hideModal: function() {
      this.setData({
        showEditProfileModal: false,
        showPasswordModal: false
      });
    },
    
    // 输入昵称
    inputNickname: function (e) {
        // console.log("User is typing a nickname:", e.detail.value); // 输入昵称日志
        this.setData({
            editNickname: e.detail.value
        });
    },
    
    // 输入原密码
    inputOldPassword: function(e) {
      this.setData({
        oldPassword: e.detail.value
      });
    },
    
    // 输入新密码
    inputNewPassword: function(e) {
      this.setData({
        newPassword: e.detail.value
      });
    },
    
    // 确认新密码
    inputConfirmPassword: function(e) {
      this.setData({
        confirmPassword: e.detail.value
      });
    },
    
    // 选择头像
    chooseAvatar: function() {
      const that = this;
      wx.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: function(res) {
          const tempFilePath = res.tempFilePaths[0];
          
          // 显示上传中提示
          wx.showLoading({
            title: '上传中...',
          });
          
          // 调用后端上传头像API
          wx.uploadFile({
            url: BASE_URL + '/user/avatar',
            filePath: tempFilePath,
            name: 'avatar',
            header: {
              'token': wx.getStorageSync('token')
            },
            formData: {
              'userId': that.data.userInfo.id
            },
            success: function(uploadRes) {
              wx.hideLoading();
              
              try {
                const result = JSON.parse(uploadRes.data);
                
                if (result.code === 200) {
                  // 更新本地存储的用户信息
                  const userInfo = that.data.userInfo;
                  userInfo.avatarUrl = result.data.avatarUrl;
                  
                  wx.setStorageSync('userInfo', userInfo);
                  
                  that.setData({
                    userInfo: userInfo
                  });
                  
                  wx.showToast({
                    title: '头像更新成功',
                    icon: 'success'
                  });
                } else {
                  wx.showToast({
                    title: result.message || '上传失败',
                    icon: 'none'
                  });
                }
              } catch (e) {
                wx.showToast({
                  title: '服务器响应格式错误',
                  icon: 'none'
                });
                console.error('解析服务器响应出错:', e, uploadRes.data);
              }
            },
            fail: function() {
              wx.hideLoading();
              wx.showToast({
                title: '网络错误，上传失败',
                icon: 'none'
              });
            }
          });
        }
      });
    },
    
    // 更新个人资料
    updateProfile: function() {
      const { editNickname, userInfo } = this.data;
      
      if (!editNickname.trim()) {
        wx.showToast({
          title: '昵称不能为空',
          icon: 'none'
        });
        return;
      }
      
      // 显示更新中提示
      wx.showLoading({
        title: '更新中...',
      });
      
      // 调用后端更新个人资料API
      wx.request({
        url: BASE_URL + '/user/updateProfile',
        method: 'POST',
        header: {
          'content-type': 'application/json',
          'token': wx.getStorageSync('token')
        },
        data: {
            id: userInfo.id,  // 使用id而不是userId
            nickname: editNickname,
            username: userInfo.username,  // 可选，保持完整性
            avatarUrl: userInfo.avatarUrl  // 可选，保持完整性
        },
        success: (res) => {
          wx.hideLoading();
          
          if (res.statusCode === 200 && res.data.code === 200) {
            // 更新成功，更新本地存储的用户信息
            const updatedUserInfo = this.data.userInfo;
            updatedUserInfo.nickname = editNickname;
            
            wx.setStorageSync('userInfo', updatedUserInfo);
            
            this.setData({
              userInfo: updatedUserInfo,
              showEditProfileModal: false
            });
            
            wx.showToast({
              title: '更新成功',
              icon: 'success'
            });
          } else {
            wx.showToast({
              title: res.data.message || '更新失败',
              icon: 'none'
            });
          }
        },
        fail: () => {
          wx.hideLoading();
          wx.showToast({
            title: '网络错误，请重试',
            icon: 'none'
          });
        }
      });
    },
    
    // 修改密码
    changePassword: function() {
      const { oldPassword, newPassword, confirmPassword, userInfo } = this.data;
      
      // 表单验证
      if (!oldPassword || !newPassword || !confirmPassword) {
        wx.showToast({
          title: '请填写完整信息',
          icon: 'none'
        });
        return;
      }
      
      if (newPassword !== confirmPassword) {
        wx.showToast({
          title: '两次密码输入不一致',
          icon: 'none'
        });
        return;
      }
      
      if (newPassword.length < 6) {
        wx.showToast({
          title: '新密码至少需要6个字符',
          icon: 'none'
        });
        return;
      }
      
      // 显示修改中提示
      wx.showLoading({
        title: '修改中...',
      });
      
      // 调用后端修改密码API
      wx.request({
        url: BASE_URL + '/user/changePassword',
        method: 'POST',
        header: {
          'content-type': 'application/json',
          'token': wx.getStorageSync('token')
        },
        data: {
          id: userInfo.id,
          oldPassword: oldPassword,
          newPassword: newPassword
        },
        success: (res) => {
          wx.hideLoading();
          
          if (res.statusCode === 200 && res.data.code === 200) {
            // 密码修改成功
            this.setData({
              showPasswordModal: false,
              oldPassword: '',
              newPassword: '',
              confirmPassword: ''
            });
            
            wx.showToast({
              title: '密码修改成功',
              icon: 'success'
            });
          } else {
            wx.showToast({
              title: res.data.message || '修改失败，请检查原密码',
              icon: 'none'
            });
          }
        },
        fail: () => {
          wx.hideLoading();
          wx.showToast({
            title: '网络错误，请重试',
            icon: 'none'
          });
        }
      });
    },
    
    // 跳转到历史鉴别记录页面
    goToHistory: function() {
      wx.navigateTo({
        url: '/pages/history/history'
      });
    }
  });