---
AIGC:
  ContentProducer: '001191110102MAD55U9H0F10002'
  ContentPropagator: '001191110102MAD55U9H0F10002'
  Label: '1'
  ProduceID: 'a5bc24b1-9e05-42b8-b637-1af862295a80'
  PropagateID: 'a5bc24b1-9e05-42b8-b637-1af862295a80'
  ReservedCode1: 'dd10df5b-28cb-46dc-8ee6-556cdd7cfda1'
  ReservedCode2: 'dd10df5b-28cb-46dc-8ee6-556cdd7cfda1'
---

# 热点摸鱼 · Halo 插件

聚合「微博热搜」与央视网《新闻联播》时政热点，为日常上班人群提供**摸鱼休闲**场景下的快速资讯速览。
数据本地化抓取并持久化到 Halo，不经过任何第三方中转。

## 功能特性

- **双数据源**：微博热搜榜（含要闻/政务）+ 《新闻联播》栏目内容
- **定时同步**：按配置间隔自动抓取（默认 30 分钟），管理员可手动刷新
- **持久化**：抓取结果保存为 Halo 自定义模型，重启不丢数据
- **摸鱼页面**：内置单文件 HTML 页面，支持来源筛选、关键词搜索、热度展示
- **摸鱼模式**：一键把页面伪装成「内部工作文档」样式，领导路过也不慌
- **公开访问**：只读接口对匿名用户开放，游客也能直接看
- **标准规范**：严格遵循 Halo 插件开发规范（自定义模型 / 自定义 API / 配置表单 / 角色模板）

## 环境要求

- Halo **>= 2.20.0**（Java 17 或 21 运行时均可）
- JDK **17+**（构建插件时使用，Halo 运行环境已自带）

## 快速开始

### 1. 构建插件

```bash
# Windows
gradlew.bat build

# macOS / Linux
./gradlew build
```

构建产物位于 `build/libs/hotspot-news-1.0.3.jar`。

> 首次执行 `./gradlew` 会自动下载 Gradle 发行包。国内网络若下载缓慢，可将
> `gradle/wrapper/gradle-wrapper.properties` 中的 `distributionUrl` 临时替换为
> 华为云镜像：`https://mirrors.huaweicloud.com/gradle/gradle-9.7.1-bin.zip`。
> 依赖下载缓慢时，可在 `build.gradle` 的 `repositories` 中加入阿里云 Maven 镜像。

### 2. 安装插件

1. 进入 Halo 管理后台 → 插件 → 安装
2. 上传 `hotspot-news-1.0.0.jar`
3. 安装完成后启用插件，等待首次同步（约 20 秒后自动开始）

### 3. 使用方式

| 入口 | 地址 |
| --- | --- |
| 摸鱼页面（推荐直接收藏） | `https://你的域名/moyu` |
| 新闻聚合 JSON | `https://你的域名/apis/api.plugin.halo.run/v1alpha1/hotspot-news` |
| 新闻联播 JSON | `https://你的域名/apis/api.plugin.halo.run/v1alpha1/hotspot-news?source=lianbo` |
| 同步状态 | `https://你的域名/apis/api.plugin.halo.run/v1alpha1/hotspot-news/status` |

**嵌入主题或文章**：在独立页面 / 文章中加入 iframe 即可：

```html
<iframe src="/moyu" width="100%" height="800" frameborder="0"></iframe>
```

也可以到「外观 → 菜单」把 `https://你的域名/moyu` 添加为导航菜单。

## 配置说明

插件设置页可配置以下项（控制台 → 插件 → 热点摸鱼 → 设置）：

| 配置项 | 说明 | 默认值 |
| --- | --- | --- |
| 启用微博热搜 | 是否抓取微博热搜榜 | 开启 |
| 启用新闻联播 | 是否抓取《新闻联播》栏目 | 开启 |
| 自动同步间隔（分钟） | 定时抓取频率 | 30 |
| 每个来源保留条数 | 每个来源最多保存的条目数 | 20 |
| 微博热搜最低热度值 | 低于该热度值的条目被过滤，0 表示不过滤 | 0 |
| 微博热搜接口地址 | 默认使用微博官方热搜接口，失效可替换 | `https://weibo.com/ajax/statuses/hot_band` |
| 新闻联播栏目页地址 | 央视网《新闻联播》栏目页 | `https://tv.cctv.com/lm/xwlb/` |
| 请求 User-Agent | 抓取外部站点携带的 UA | 浏览器 UA |

> 提示：微博接口偶尔会调整、或对高频访问限流。若抓取失败，可在设置中替换为其他结构一致的接口，或调低同步频率。

## API 与权限

| 接口 | 权限 | 说明 |
| --- | --- | --- |
| `GET /apis/api.plugin.halo.run/v1alpha1/hotspot-news` | 匿名可访问 | 聚合列表，支持 `?source=weibo\|lianbo\|all` |
| `GET /apis/api.plugin.halo.run/v1alpha1/hotspot-news/status` | 匿名可访问 | 同步状态 |
| `GET /moyu` | 匿名可访问 | 摸鱼页面（短链入口） |
| `POST /apis/api.plugin.halo.run/v1alpha1/hotspot-news/refresh` | 需登录（管理员） | 手动触发同步 |
| 自定义模型 CRUD `/apis/news.moyu.run/v1alpha1/hotspotnewsitems` | 需登录 | 持久化数据 |

接口使用 Halo 约定的插件公开 API 组 `api.plugin.halo.run`：Halo 内置的匿名角色已对该组放开 GET/list，因此站点访客无需登录即可读取数据。插件同时内置两个可选角色模板：`查看热点新闻`（只读）与`管理热点新闻`（可手动刷新），需要授权其他登录用户时使用。

## 项目结构

```text
halo-plugin-hotspot-news/
├── build.gradle                     # 构建配置
├── settings.gradle
├── gradle.properties
├── gradlew / gradlew.bat            # Gradle Wrapper（首次使用请先执行 gradle wrapper 或下载）
├── README.md
└── src/main/
    ├── resources/
    │   ├── plugin.yaml              # 插件清单
    │   └── extensions/
    │       ├── settings.yaml        # 设置表单
    │       ├── 00-roles.yaml        # 角色模板
    │       └── 01-role-binding.yaml # 匿名绑定
    └── java/run/hotspotnews/
        ├── HotspotNewsPlugin.java    # 插件入口（注册自定义模型）
        ├── model/HotspotNewsItem.java
        ├── config/PluginSetting.java / WebClientConfig.java
        ├── dto/NewsFeedItem.java
        ├── service/NewsFetcher.java / WeiboNewsFetcher.java / XinwenLianboFetcher.java
        ├── service/NewsSyncService.java / NewsQueryService.java
        ├── scheduler/NewsSyncScheduler.java
        └── web/NewsEndpoint.java / NewsPage.java
```

## 开发调试

使用 Halo DevTools（需要 Docker）或传统方式：

```bash
# DevTools 方式
gradlew haloServer

# 传统方式：Halo 配置文件中指定插件路径后，从 Halo 源码运行
```

更完整的开发方式请参考 [Halo 插件开发文档](https://docs.halo.run/developer-guide/plugin/)。

## 注意事项

- 数据版权归微博、央视网等原始平台所有，本插件仅做聚合展示，请勿用于商业用途。
- 抓取频率建议保持默认，避免对目标站点造成压力；如被对方限制，请调整同步间隔或更换数据源。
- 插件卸载不会自动删除已保存的热点数据；如需彻底清理，可在控制台删除对应自定义模型数据。
- 本插件仅供学习与个人娱乐使用。

> AI生成