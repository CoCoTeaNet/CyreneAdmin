<template>
  <div class="home-container">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">欢迎来到 Cyrene Admin</span>
        </div>
      </template>
      <div class="welcome-content">
        <p class="subtitle">一个现代化的管理后台系统</p>
        <p class="description">
          Cyrene Admin 是一个基于 Solon 和 Vue 3 的现代化管理后台系统。
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
    <el-card class="guide-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">快速开始指南</span>
        </div>
      </template>
      <el-steps :active="3" finish-status="success" align-center simple class="guide-steps">
        <el-step title="注册账户" description="创建您的管理员账户" />
        <el-step title="配置权限" description="设置角色和权限" />
        <el-step title="管理内容" description="开始管理您的业务数据" />
        <el-step title="监控系统" description="查看系统日志和状态" />
      </el-steps>
    </el-card>

    <!-- 项目信息 -->
    <el-card class="info-card" shadow="never">
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
    desc: '管理用户账户',
    icon: 'User',
    color: '#409EFF'
  },
  {
    title: '角色权限',
    desc: '配置角色权限',
    icon: 'Avatar',
    color: '#67C23A'
  },
  {
    title: '菜单管理',
    desc: '配置系统菜单',
    icon: 'Grid',
    color: '#E6A23C'
  },
  {
    title: '系统日志',
    desc: '查看操作日志',
    icon: 'Document',
    color: '#F56C6C'
  }
];
</script>

<style scoped>
.home-container {
  min-height: 100vh;
}

.welcome-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.guide-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.info-card {
  margin-top: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.welcome-content {
  text-align: center;
}

.subtitle {
  font-size: 16px;
  color: #606266;
  margin-bottom: 15px;
  font-weight: 500;
  text-align: center;
}

.description {
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
  margin-bottom: 30px;
}

.features {
  margin: 20px 0;
  text-align: center;
}

.feature-item {
  text-align: center;
  padding: 20px 10px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.feature-item:hover {
  background-color: #f5f7fa;
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
  margin: 12px 0 8px;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
}

.feature-item p {
  margin: 5px 0;
  color: #7a7c85;
  font-size: 14px;
  line-height: 1.5;
  text-align: center;
}

.guide-steps {
  margin: 30px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.features-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-step__head) {
  width: 30px;
  height: 30px;
  border: 2px solid #d9ecff;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-step__title) {
  font-weight: 500;
  text-align: center;
  font-size: 14px;
}

:deep(.el-step__description) {
  padding-top: 4px;
  font-size: 12px;
  color: #909399;
  text-align: center;
  line-height: 1.4;
}

:deep(.el-tag) {
  border: none;
  border-radius: 6px;
}
</style>