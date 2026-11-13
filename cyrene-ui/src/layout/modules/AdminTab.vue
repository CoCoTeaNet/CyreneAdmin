<template>
  <div class="tabs-wrapper">
    <el-scrollbar>
      <div class="tabs-container">
        <div
          v-for="item in menuStore.tabItems"
          :key="item.url"
          :class="['tab-item', { active: item.isActive }]"
          @click="onClick(item)"
          @contextmenu.prevent="openContextMenu($event, item)"
        >
          <span class="tab-dot" v-if="item.isActive"></span>
          <span class="tab-title">{{ translate(item.name) }}</span>
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
          <el-dropdown-item @click="refresh">{{ t('tab.refresh') }}</el-dropdown-item>
          <el-dropdown-item @click="onCloseAll">{{ t('tab.closeAll') }}</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 标签右键菜单 -->
    <Teleport to="body">
      <div v-if="ctx.visible" class="tab-ctx-overlay" @click="closeCtx" @contextmenu.prevent="closeCtx">
        <ul class="tab-ctx-menu" :style="{ left: ctx.x + 'px', top: ctx.y + 'px' }" @click.stop>
          <li @click="ctxRefresh">
            <el-icon><Refresh/></el-icon>{{ t('tab.ctxRefresh') }}
          </li>
          <li :class="{ disabled: single }" @click="ctxClose">
            <el-icon><Close/></el-icon>{{ t('tab.close') }}
          </li>
          <li :class="{ disabled: single }" @click="ctxCloseOthers">
            <el-icon><Close/></el-icon>{{ t('tab.closeOthers') }}
          </li>
          <li @click="ctxMaximize">
            <el-icon><FullScreen/></el-icon>
            {{ menuStore.isMaximize ? t('tab.exitMaximize') : t('tab.maximize') }}
          </li>
          <li @click="ctxNewWindow">
            <el-icon><TopRight/></el-icon>{{ t('tab.openInNewWindow') }}
          </li>
        </ul>
      </div>
    </Teleport>
  </div>
</template>

<script lang="ts" setup>
import {ArrowDown, Close, FullScreen, Refresh, TopRight} from "@element-plus/icons-vue";
import {router} from "@/router";
import {useRoute} from "vue-router";
import {computed, onBeforeUnmount, onMounted, reactive} from "vue";
import {useMenuStore} from "@/stores/menu.ts";
import {useI18n} from "vue-i18n";
import {translate} from "@/i18n";

const {t} = useI18n();

const menuStore = useMenuStore();
const route = useRoute();

const single = computed(() => menuStore.tabItems.length <= 1);

const ctx = reactive<{ visible: boolean; x: number; y: number; tab: TabItem | null }>({
  visible: false, x: 0, y: 0, tab: null
});

onMounted(() => {
  menuStore.addTabItem({name: JSON.parse(JSON.stringify(route.meta)).title, url: route.path, isActive: true});
});

onBeforeUnmount(() => {
  closeCtx();
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

const onEsc = (e: KeyboardEvent) => {
  if (e.key === 'Escape') closeCtx();
}

const openContextMenu = (e: MouseEvent, item: TabItem) => {
  const MENU_W = 190;
  const MENU_H = 190;
  ctx.tab = item;
  ctx.x = Math.max(8, Math.min(e.clientX, window.innerWidth - MENU_W - 8));
  ctx.y = Math.max(8, Math.min(e.clientY, window.innerHeight - MENU_H - 8));
  ctx.visible = true;
  window.addEventListener('keydown', onEsc);
}

const closeCtx = () => {
  ctx.visible = false;
  ctx.tab = null;
  window.removeEventListener('keydown', onEsc);
}

const ctxRefresh = () => {
  const tab = ctx.tab;
  closeCtx();
  if (tab) router.push({path: tab.url}).then(() => window.location.reload());
}

const ctxClose = () => {
  const tab = ctx.tab;
  closeCtx();
  if (tab && !single.value) menuStore.removeTabItem(tab.id!);
}

const ctxCloseOthers = () => {
  const tab = ctx.tab;
  closeCtx();
  if (tab && !single.value) menuStore.removeOtherTabItems(tab.id!);
}

const ctxMaximize = () => {
  closeCtx();
  menuStore.toggleMaximize();
}

const ctxNewWindow = () => {
  const tab = ctx.tab;
  closeCtx();
  if (tab) window.open(`${location.origin}${location.pathname}#${tab.url}`, '_blank');
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

/* 右键菜单 */
.tab-ctx-overlay {
  position: fixed;
  inset: 0;
  z-index: 3000;
}

.tab-ctx-menu {
  position: fixed;
  z-index: 3001;
  min-width: 180px;
  margin: 0;
  padding: 4px;
  list-style: none;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
}

.tab-ctx-menu li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  cursor: pointer;
}

.tab-ctx-menu li:hover {
  background: var(--el-fill-color-light);
  color: var(--el-color-primary);
}

.tab-ctx-menu li.disabled {
  opacity: 0.4;
  pointer-events: none;
}

.tab-ctx-menu li .el-icon {
  font-size: 15px;
}
</style>