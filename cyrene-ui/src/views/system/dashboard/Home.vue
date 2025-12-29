<template>
  <div class="home-container">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span class="title">欢迎来到 Cyrene Admin</span>
        </div>
      </template>
      <div class="welcome-content">
        <p class="subtitle">一个现代化的管理后台系统</p>
        <p class="description">
          Cyrene Admin 是一个基于 Solon 和 Vue 3 的现代化管理后台系统，采用前后端分离架构，
          提供了完整的用户管理、角色权限、菜单配置等功能，帮助您快速构建企业级应用。
        </p>
        <div class="features">
          <el-row :gutter="20">
            <el-col :span="6" v-for="(feature, index) in features" :key="index">
              <div class="feature-item">
                <div class="feature-icon" :style="{ backgroundColor: feature.color }">
                  <el-icon :size="24">
                    <component :is="feature.icon"></component>
                  </el-icon>
                </div>
                <h4>{{ feature.title }}</h4>
                <p>{{ feature.desc }}</p>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>

    <!-- 快速开始指南 -->
    <el-card class="guide-card">
      <template #header>
        <div class="card-header">
          <span class="title">快速开始指南</span>
        </div>
      </template>
      <el-steps :active="3" finish-status="success" align-center class="guide-steps">
        <el-step title="注册账户" description="创建您的管理员账户" />
        <el-step title="配置权限" description="设置角色和权限" />
        <el-step title="管理内容" description="开始管理您的业务数据" />
        <el-step title="监控系统" description="查看系统日志和状态" />
      </el-steps>
    </el-card>

    <!-- 项目信息 -->
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span class="title">项目信息</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目名称">
          <el-tag type="info">Cyrene Admin</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前版本">
          <el-tag type="success">v2.0.0</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="技术栈">
          <el-tag type="warning">Vue 3 + TypeScript</el-tag>
          <el-tag type="warning">Element Plus</el-tag>
          <el-tag type="warning">Solon</el-tag>
          <el-tag type="warning">SQLtoy</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开发状态">
          <el-tag type="danger">开发中</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="项目特点">
          <div class="features-tags">
            <el-tag type="info">前后端分离</el-tag>
            <el-tag type="info">响应式设计</el-tag>
            <el-tag type="info">权限管理</el-tag>
            <el-tag type="info">国际化支持</el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="项目目标">
          提供一个高效、安全、易用的管理后台解决方案，帮助开发者快速构建企业级应用
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { 
  User, 
  Avatar, 
  Grid, 
  Document
} from '@element-plus/icons-vue';

interface Feature {
  title: string;
  desc: string;
  icon: string;
  color: string;
}

const features: Feature[] = [
  {
    title: '用户管理',
    desc: '管理系统的用户账户、权限和角色分配',
    icon: 'User',
    color: '#409EFF'
  },
  {
    title: '角色权限',
    desc: '灵活配置角色权限，实现精细化权限控制',
    icon: 'Avatar',
    color: '#67C23A'
  },
  {
    title: '菜单管理',
    desc: '动态配置系统菜单，支持多级菜单结构',
    icon: 'Grid',
    color: '#E6A23C'
  },
  {
    title: '系统日志',
    desc: '记录系统操作日志，便于问题追踪和审计',
    icon: 'Document',
    color: '#F56C6C'
  }
];
</script>

<style scoped>
.home-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.welcome-card {
  margin-bottom: 20px;
}

.guide-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.welcome-content {
  text-align: center;
}

.subtitle {
  font-size: 16px;
  color: #606266;
  margin-bottom: 15px;
}

.description {
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
  margin-bottom: 30px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.features {
  margin-top: 20px;
}

.feature-item {
  text-align: center;
  padding: 20px 10px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
}

.feature-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  color: white;
}

.feature-item h4 {
  margin: 10px 0;
  color: #303133;
  font-size: 16px;
}

.feature-item p {
  margin: 5px 0;
  color: #909399;
  font-size: 14px;
}

.guide-steps {
  margin: 30px 0;
}

.info-card {
  margin-top: 20px;
}

.features-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

:deep(.el-descriptions__header) {
  margin-bottom: 20px;
}

:deep(.el-descriptions__cell) {
  padding-bottom: 12px;
}

:deep(.el-descriptions__content) {
  word-break: break-all;
}
</style>