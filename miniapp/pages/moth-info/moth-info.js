Page({
    data: {
      currentTab: 0,
      moths: [
        {
          name: "草地贪夜蛾",
          scientific: "Spodoptera frugiperda"
        },
        {
          name: "斜纹夜蛾",
          scientific: "Spodoptera litura"
        },
        {
          name: "甜菜夜蛾",
          scientific: "Spodoptera exigua"
        },
        {
          name: "劳氏粘虫",
          scientific: "Leucania loreyi"
        },
        {
          name: "黄地老虎",
          scientific: "Agrotis segetum"
        }
      ]
    },
  
    onLoad: function() {
      wx.setNavigationBarTitle({
        title: '飞蛾相关知识'
      });
    },
  
    // 选项卡点击切换
    switchTab: function(e) {
      const index = e.currentTarget.dataset.index;
      this.setData({
        currentTab: index
      });
    },
  
    // 滑动切换
    swiperChange: function(e) {
      const current = e.detail.current;
      this.setData({
        currentTab: current
      });
    }
  });