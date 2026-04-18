# 极行桌面 - GitHub上传与构建指南

## 📋 当前状态
✅ 已修复所有编译错误
✅ 已添加签名配置和签名密钥
✅ 已添加多分辨率适配资源
✅ 已创建GitHub Actions工作流
✅ 代码已提交到本地Git仓库

---

## 🚀 上传到GitHub步骤

### 步骤1: 创建GitHub仓库
1. 登录 [GitHub](https://github.com)
2. 点击右上角 `+` → `New repository`
3. 仓库名称: `JixingLauncher`
4. 选择 `Private` 或 `Public`
5. **不要**勾选 "Add a README file"
6. 点击 `Create repository`

### 步骤2: 添加GitHub Secret (签名密钥)
1. 进入仓库 → `Settings` → `Secrets and variables` → `Actions`
2. 点击 `New repository secret`
3. Name: `KEYSTORE_BASE64`
4. Value: (复制下面的Base64内容)

```
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
```

### 步骤3: 推送代码到GitHub

在本地终端执行以下命令：

```bash
# 进入项目目录
cd 极行桌面项目/JixingLauncher

# 添加远程仓库 (替换 YOUR_USERNAME 为你的GitHub用户名)
git remote add origin https://github.com/YOUR_USERNAME/JixingLauncher.git

# 推送代码
git branch -M main
git push -u origin main
```

### 步骤4: 触发构建
推送代码后，GitHub Actions会自动触发构建：
- 进入仓库 → `Actions` 标签页
- 可以看到构建进度

### 步骤5: 下载APK
构建完成后：
- 进入 `Actions` → 选择最新的workflow run
- 在 `Artifacts` 区域下载：
  - `jixing-launcher-debug` - Debug版本
  - `jixing-launcher-release` - Release签名版本

---

## 📦 或者：手动上传ZIP

如果不想用Git命令，可以直接下载项目ZIP上传：

1. 项目文件夹：`极行桌面项目/JixingLauncher`
2. 在GitHub网页直接拖拽上传

---

## 🔧 项目信息

| 项目 | 值 |
|------|-----|
| 应用ID | com.jixing.launcher |
| 最低SDK | 26 (Android 8.0) |
| 目标SDK | 34 (Android 14) |
| 版本号 | 1.0.0 |
| 签名密钥密码 | qq900236 |
| 密钥别名 | jixing |

---

## 📱 多分辨率支持

| 设备类型 | smallestWidth | 列数 |
|---------|--------------|------|
| 小屏手机 | sw360dp | 3-4列 |
| 普通手机 | sw480dp | 4-5列 |
| 平板 | sw600dp | 5-6列 |
| 大屏/车载 | sw720dp | 6-7列 |

---

## ⚠️ 注意事项

1. **签名密钥安全**: keystore文件已包含在仓库中，建议使用GitHub Secret方式存储
2. **构建时间**: 首次构建需要下载Gradle依赖，约需5-10分钟
3. **构建失败**: 检查Actions日志，可能是网络或依赖问题
