<template>
  <el-menu class="new-el-menu--sidebar"
           :default-active="menuState.defaultActive"
           :default-openeds="menuState.defaultOpened"
           :collapse="menuStore.isCollapseMenu"
           :unique-opened="true">
    <!-- LOGO -->
    <div v-if="!menuStore.isCollapseMenu" class="logo">
      <img src="@/assets/logo.png" style="width: 36px;margin-right: 3px" alt="login-logo">
      <h3>CyreneAdmin</h3>
    </div>
    <div v-else style="width: 100%;cursor: pointer;display: flex;justify-content: center;margin-top: 1em">
      <img src="@/assets/logo.png" style="width: 36px" alt="logo">
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
}

/* 解决侧边栏折叠卡顿的问题 */
.new-el-menu--sidebar:not(.el-menu--collapse) {
  width: 220px;
}
</style>
