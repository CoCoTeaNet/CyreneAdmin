<script setup lang="ts">
import {computed} from "vue";
import BaseLayout from "@/layout/BaseLayout.vue";
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import {useUserStore} from "@/stores/user.ts";
import {useMenuStore} from "@/stores/menu.ts";
import {onMounted} from "vue";
import i18n, {LOCALE_ZH} from "@/i18n";

const userStore = useUserStore();
const menuStore = useMenuStore();

// Element Plus 组件库语言跟随 i18n 语言切换
const elementLocale = computed(() => i18n.global.locale.value === LOCALE_ZH ? zhCn : en);

// 读取缓存，以免用户F5刷新登录失效
let userInfo = JSON.parse(`${localStorage.getItem("userInfo")}`);
if (userInfo) {
  userStore.setUserInfo(userInfo);
}

onMounted(() => {
  // 初始化tabItems
  menuStore.loadTabItems();
});
</script>

<template>
  <el-config-provider :locale="elementLocale">
    <div class="base-layout">
      <el-scrollbar>
        <base-layout/>
      </el-scrollbar>
    </div>
  </el-config-provider>
</template>

<style>
div, h2, html, body {
  margin: 0;
  padding: 0;
  font-family: Noto Sans, Noto Sans HK, Noto Sans JP, Noto Sans KR, Noto Sans SC, Noto Sans TC, sans-serif;
}

.app-background {
  width: 100%;
  height: 100%;
  position: fixed;
}

p {
  font-weight: 500;
}
</style>