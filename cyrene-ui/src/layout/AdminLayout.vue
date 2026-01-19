<template>
  <el-container style="height: 100vh;">
    <el-aside width="300">
      <NavMenu style="height: 100%;overflow: auto" />
    </el-aside>

    <el-container>
      <el-header class="layout-box-shadow"><admin-header /></el-header>

      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>

    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import NavMenu from "./modules/NavMenu.vue";
import AdminHeader from "./modules/AdminHeader.vue";
</script>

<style scoped>
/* 核心动画设置 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

/* 进入时的状态：从下方 20px 处浮现 */
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

/* 离开时的状态：向消失并稍微上浮（或保持原地消失） */
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>

<style>
.layout-box-shadow {
  background-color: #fff; /* 确保有背景色 */
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  
  /* 关键属性：防止阴影被下方内容遮挡 */
  position: relative;
  z-index: 10; 
}
</style>
