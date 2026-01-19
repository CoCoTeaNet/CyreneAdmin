<template>
  <div class="tabs-wrapper">
    <el-scrollbar>
      <div class="tabs-container">
        <div 
          v-for="item in menuStore.tabItems" 
          :key="item.url" 
          :class="['tab-item', { active: item.isActive }]"
          @click="onClick(item)"
        >
          <span class="tab-dot" v-if="item.isActive"></span>
          <span class="tab-title">{{ item.name }}</span>
          <el-icon 
            v-if="menuStore.tabItems.length > 1" 
            class="tab-close" 
            @click.stop="menuStore.removeTabItem(item.id)"
          >
            <close/>
          </el-icon>
        </div>
      </div>
    </el-scrollbar>
    
    <el-dropdown trigger="click">
      <div class="tab-more">
        <el-icon><arrow-down/></el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="refresh">刷新当前</el-dropdown-item>
          <el-dropdown-item @click="onCloseAll">关闭所有</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script lang="ts" setup>
import {ArrowDown, Close} from "@element-plus/icons-vue";
import {router} from "@/router";
import {useRoute} from "vue-router";
import {onMounted} from "vue";
import {useMenuStore} from "@/stores/menu.ts";

const menuStore = useMenuStore();
const route = useRoute();

onMounted(() => {
  menuStore.addTabItem({name: JSON.parse(JSON.stringify(route.meta)).title, url: route.path, isActive: true});
});

const onClick = (obj: any) => {
  router.push({path: obj.url});
  menuStore.tabItems.forEach((item: TabItem) => {
    item.isActive = item.id == obj.id;
  });
}

const onCloseAll = () => {
  menuStore.initTabItems();
}

const refresh = () => {
  window.location.reload();
}
</script>

<style scoped>
.tabs-wrapper {
  display: flex;
  align-items: center;
  overflow: hidden;
  height: 40px;
}

.tabs-container {
  display: flex;
  gap: 6px;
  padding: 0 4px;
}

.tab-item {
  position: relative;
  display: flex;
  align-items: center;
  height: 32px;
  padding: 0 12px;
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
  color: var(--el-text-color-regular);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
  border: 1px solid transparent;
  white-space: nowrap;
}

.tab-item:hover {
  background: var(--el-fill-color-light);
  color: var(--el-color-primary);
}

/* 激活状态 */
.tab-item.active {
  background: #fff;
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-7);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.tab-dot {
  width: 6px;
  height: 6px;
  background: var(--el-color-primary);
  border-radius: 50%;
  margin-right: 8px;
}

.tab-close {
  margin-left: 8px;
  font-size: 12px;
  border-radius: 50%;
  padding: 2px;
  transition: all 0.2s;
}

.tab-close:hover {
  background-color: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.tab-more {
  padding: 0 10px;
  cursor: pointer;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
}
</style>