<template>
  <div class="home-container">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">{{ t('home.welcome') }}</span>
        </div>
      </template>
      <div class="welcome-content">
        <p class="subtitle">{{ t('home.subtitle') }}</p>
        <p class="description">
          {{ t('home.description') }}
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
                <h4>{{ t(feature.titleKey) }}</h4>
                <p>{{ t(feature.descKey) }}</p>
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
          <span class="title">{{ t('home.guide') }}</span>
        </div>
      </template>
      <el-steps :active="3" finish-status="success" align-center simple class="guide-steps">
        <el-step :title="t('home.step1Title')" :description="t('home.step1Desc')" />
        <el-step :title="t('home.step2Title')" :description="t('home.step2Desc')" />
        <el-step :title="t('home.step3Title')" :description="t('home.step3Desc')" />
        <el-step :title="t('home.step4Title')" :description="t('home.step4Desc')" />
      </el-steps>
    </el-card>

    <!-- 项目信息 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">{{ t('home.projectInfo') }}</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="t('home.projectName')">
          <el-tag type="info">Cyrene Admin</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('home.currentVersion')">
          <el-tag type="success">v2.0.0</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('home.techStack')">
          <el-tag type="warning">Vue 3 + TypeScript</el-tag>
          <el-tag type="warning">Element Plus</el-tag>
          <el-tag type="warning">Solon</el-tag>
          <el-tag type="warning">SQLtoy</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('home.devStatus')">
          <el-tag type="danger">{{ t('home.inDevelopment') }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('home.features')">
          <div class="features-tags">
            <el-tag type="info">{{ t('home.frontBackSep') }}</el-tag>
            <el-tag type="info">{{ t('home.responsive') }}</el-tag>
            <el-tag type="info">{{ t('home.permission') }}</el-tag>
            <el-tag type="info">{{ t('home.i18n') }}</el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item :label="t('home.goal')">
          {{ t('home.goalDesc') }}
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
import {useI18n} from "vue-i18n";

const {t} = useI18n();

interface Feature {
  titleKey: string;
  descKey: string;
  icon: string;
  color: string;
}

const features: Feature[] = [
  {
    titleKey: 'home.fUser',
    descKey: 'home.fUserDesc',
    icon: 'User',
    color: '#409EFF'
  },
  {
    titleKey: 'home.fRole',
    descKey: 'home.fRoleDesc',
    icon: 'Avatar',
    color: '#67C23A'
  },
  {
    titleKey: 'home.fMenu',
    descKey: 'home.fMenuDesc',
    icon: 'Grid',
    color: '#E6A23C'
  },
  {
    titleKey: 'home.fLog',
    descKey: 'home.fLogDesc',
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