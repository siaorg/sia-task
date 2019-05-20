# gantry-task微服务任务调度平台前端部署启动

## 前言

该项目基于node环境运行，如未安装node，请先安装node

传送门：https://nodejs.org/en/download/

## 技术栈

vue2 + vuex + vue-router + webpack + ES6 + less + axios + element-ui

## 项目运行

npm install 或 cnpm install(推荐)

注：CNPM 为淘宝镜像 如未安装，传送门：HTTP://NPM.TAOBAO.ORG/
npm run dev

访问: http://localhost:8083

## 项目打包

npm run build

注：可以更改DIST/STATIC文件夹下面的SITE.MAP.JS文件进行环境的配置（IP：PORT形式）
1、CESHI_API_HOST -> 后端服务地址

2、CESHI_API_HOST_LOG -> kabana 日志连接地址