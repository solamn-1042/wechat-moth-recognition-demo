# 微信端页面预览

README 中的五张图片由仓库 `miniapp/pages/` 的 WXML、WXSS 和页面 JS 初始数据生成，视口为 390 × 844 CSS 像素，截图倍率为 2。浏览器适配脚本只用于文档展示，不改变小程序业务代码。

| 图片 | 页面源码 | 展示状态 |
|---|---|---|
| `images/home.png` | `pages/index/index` | 未登录首页 |
| `images/upload.png` | `pages/upload/upload` | 选中仓库的草地贪夜蛾示例图 |
| `images/result.png` | `pages/upload/upload` | 实际模型推理结果，草地贪夜蛾，86.3% |
| `images/knowledge.png` | `pages/moth-info/moth-info` | 首个种类的资料 |
| `images/login.png` | `pages/login/login` | 未填写账号的登录表单 |

结果图片及置信度来自 `tools/preview/infer_sample.py` 对仓库权重的实际调用，原始响应摘要保存在 [inference-sample.json](images/inference-sample.json)。此数值是单张示例的检测置信度，不是模型准确率。

## 如何重新生成

在仓库根目录，使用已安装 `inference/requirements.txt` 的 Python：

```powershell
python tools/preview/infer_sample.py
Set-Location tools/preview
npm install
npx playwright install chromium
npm run render
```

若本机已安装 Microsoft Edge，可省略浏览器下载，在运行前设置 `$env:PREVIEW_BROWSER_CHANNEL='msedge'`。截图仍写到仓库 `docs/images/`。只更新布局时可直接复用已提交的推理 fixture，不必再次加载模型。

## 渲染范围

脚本读取原页面数据和样式，适配 `view/text/image`、条件渲染、列表、rpx 以及当前资料选项卡；按原图比例展示标注结果。导航条为基于 `app.json` 的展示外壳。源码加载在隔离的 JS 上下文中，仅提取 `Page` 定义，不执行登录或上传业务。

这些图片是源码驱动的浏览器页面预览，并非微信模拟器或真机截图。浏览器默认控件、字体、滚动与微信原生组件可能有差异；相机、微信登录、网络上传与历史记录交互不由此脚本验证。此次没有填充虚构账号或历史识别记录。真实三端操作按主 README 的运行步骤验证。
