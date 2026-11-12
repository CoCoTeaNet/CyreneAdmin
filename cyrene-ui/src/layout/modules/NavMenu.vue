<template>
  <el-menu class="new-el-menu--sidebar"
           :class="{ 'is-label-fading': menuStore.isLabelFading }"
           :default-active="menuState.defaultActive"
           :default-openeds="menuState.defaultOpened"
           :collapse="menuStore.isCollapseMenu"
           :collapse-transition="false"
           :unique-opened="true">
    <!-- LOGO -->
    <div class="logo" :class="{ 'logo--collapsed': menuStore.isCollapseMenu }">
      <img src="@/assets/logo.png" alt="login-logo">
      <h3>CyreneAdmin</h3>
    </div>
    <!-- 菜单渲染 -->
    <child-menu :menu-list="menuStore.menus"/>
  </el-menu>
</template>

<script setup lang="ts">
import {useRoute} from "vue-router";
import {computed} from "vue";
import ChildMenu from "@/layout/modules/ChildMenu.vue";
import {useUserStore} from "@/stores/user.ts";
import {useMenuStore} from "@/stores/menu.ts";
import { useWindowSize } from "@vueuse/core";

const userStore = useUserStore();
const menuStore = useMenuStore();
const route = useRoute();

/**
 * 根据路由路径动态设置当前菜单的状态
 */
let menuState = computed(() => {
  initMenu();
  const path = route.path;
  const routes = userStore.userinfo.menuList;
  let state = {defaultOpened: [""], defaultActive: ""};
  if (routes) {
    let stack: Array<string> = [];
    for (let i in routes) {
      let route = routes[i];
      let len = stack.length;
      dfs(route, path, stack);
      if (!route.id) {
        console.error('路由配置错误，请检查菜单路径');
      }
      if (len < stack.length) {
        // 默认打开的页面
        state.defaultOpened = [routes[i].id + ''];
        break;
      }
      if (path === route.routerPath) {
        stack.push(route.id + '');
      }
    }
    if (stack.length > 0) {
      state.defaultActive = stack[0];
    }
  }
  return state;
});

const dfs = (root: MenuModel, path: string, stack: Array<string>) => {
  if (root.children) {
    let len = stack.length;
    for (let i in root.children) {
      let r = root.children[i];
      if (path === r.routerPath) {
        stack.push(r.id + '');
        return;
      }
      dfs(r, path, stack);
      if (len < stack.length) {
        return;
      }
    }
  }
}

const initMenu = () => {
  let winSize = useWindowSize();
  if (winSize.width.value <= 1200) {
    menuStore.updateCollapseMenu(true);
  } else {
    menuStore.updateCollapseMenu(false);
  }
}
</script>

<style scoped lang="css">
.logo {
  display: flex;
  align-items: center;
  color: rgb(55, 76, 82);
  padding: 0.5em 0 0.5em 1em;
  user-select: none;
}

.logo img {
  width: 36px;
  flex: none;
  margin-right: 3px;
}

/* 标题随侧边栏宽度收放被 flex 裁剪，展开时淡入 */
.logo h3 {
  flex: 0 1 auto;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  transition: opacity 0.15s ease;
}

.logo:not(.logo--collapsed) h3 {
  animation: sidebar-label-in 0.28s ease 0.12s backwards;
}

.logo--collapsed {
  padding-left: 14px;
}

.logo--collapsed img {
  margin-right: 0;
}

/* 折叠后剩余空间仍会留给 flex 子项，须显式归零避免文字残片 */
.logo--collapsed h3 {
  flex: 0 0 0;
  max-width: 0;
  opacity: 0;
}
</style>

<style lang="css">
/* 侧边栏菜单动效：宽度由 .admin-aside 单一驱动，菜单铺满跟随，避免双重过渡 */
.new-el-menu--sidebar,
.new-el-menu--sidebar.el-menu--collapse {
  width: 100%;
}

.new-el-menu--sidebar .el-sub-menu__icon-arrow {
  transition: transform 0.34s cubic-bezier(0.4, 0, 0.2, 1);
}

.new-el-menu--sidebar .el-collapse-transition-enter-active {
  transition: max-height 0.34s cubic-bezier(0.4, 0, 0.2, 1),
  padding-top 0.34s cubic-bezier(0.4, 0, 0.2, 1),
  padding-bottom 0.34s cubic-bezier(0.4, 0, 0.2, 1);
}

.new-el-menu--sidebar .el-collapse-transition-leave-active {
  transition: max-height 0.28s cubic-bezier(0.4, 0, 0.2, 1),
  padding-top 0.28s cubic-bezier(0.4, 0, 0.2, 1),
  padding-bottom 0.28s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 子菜单展开时逐项 stagger 滑入，收起时整体淡出 */
.new-el-menu--sidebar .el-collapse-transition-enter-active > li {
  animation: sidebar-item-in 0.3s cubic-bezier(0.22, 1, 0.36, 1) backwards;
}

.new-el-menu--sidebar .el-collapse-transition-enter-active > li:nth-child(2) {
  animation-delay: 0.04s;
}

.new-el-menu--sidebar .el-collapse-transition-enter-active > li:nth-child(3) {
  animation-delay: 0.08s;
}

.new-el-menu--sidebar .el-collapse-transition-enter-active > li:nth-child(4) {
  animation-delay: 0.12s;
}

.new-el-menu--sidebar .el-collapse-transition-enter-active > li:nth-child(5) {
  animation-delay: 0.16s;
}

.new-el-menu--sidebar .el-collapse-transition-enter-active > li:nth-child(6) {
  animation-delay: 0.2s;
}

.new-el-menu--sidebar .el-collapse-transition-leave-active > li {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.new-el-menu--sidebar .el-collapse-transition-leave-to > li {
  opacity: 0;
  transform: translateY(-4px);
}

@keyframes sidebar-item-in {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
}

@keyframes sidebar-label-in {
  from {
    opacity: 0;
  }
}

/* 菜单文字覆盖 EP 折叠时的瞬间隐藏：保持可见，由 flex 裁剪随宽度擦除/揭示 */
.new-el-menu--sidebar.el-menu > .el-menu-item > span,
.new-el-menu--sidebar.el-menu > .el-sub-menu > .el-sub-menu__title > span,
.new-el-menu--sidebar.el-menu > .el-menu-item-group > ul > .el-sub-menu > .el-sub-menu__title > span {
  display: inline-block;
  flex: 0 1 auto;
  min-width: 0;
  width: auto;
  height: auto;
  visibility: visible;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  transition: opacity 0.15s ease;
}

.new-el-menu--sidebar.el-menu:not(.el-menu--collapse) > .el-menu-item > span,
.new-el-menu--sidebar.el-menu:not(.el-menu--collapse) > .el-sub-menu > .el-sub-menu__title > span,
.new-el-menu--sidebar.el-menu:not(.el-menu--collapse) > .el-menu-item-group > ul > .el-sub-menu > .el-sub-menu__title > span {
  animation: sidebar-label-in 0.28s ease 0.1s backwards;
}

/* 嵌套子菜单长名称省略号收尾，避免溢出面板边缘 */
.new-el-menu--sidebar .el-menu--inline .el-menu-item > span,
.new-el-menu--sidebar .el-menu--inline .el-sub-menu__title > span {
  flex: 0 1 auto;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  transition: opacity 0.15s ease;
}

/* 折叠第一阶段：文字统一淡出，随后宽度再收拢 */
.new-el-menu--sidebar.is-label-fading > .el-menu-item > span,
.new-el-menu--sidebar.is-label-fading > .el-sub-menu > .el-sub-menu__title > span,
.new-el-menu--sidebar.is-label-fading > .el-menu-item-group > ul > .el-sub-menu > .el-sub-menu__title > span,
.new-el-menu--sidebar.is-label-fading .el-menu--inline .el-menu-item > span,
.new-el-menu--sidebar.is-label-fading .el-menu--inline .el-sub-menu__title > span,
.new-el-menu--sidebar.is-label-fading .logo h3 {
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .admin-aside,
  .logo h3,
  .new-el-menu--sidebar,
  .new-el-menu--sidebar * {
    transition: none !important;
    animation: none !important;
  }
}
</style>
