<template>
  <el-row align="middle" class="header-row">
    <el-col :span="20" style="display: flex">
      <el-space>
        <el-button link @click="menuStore.setCollapseMenu">
          <template #icon>
            <el-icon class="mouse-over right-item" :size="iconSize">
              <expand v-if="menuStore.isCollapseMenu"/>
              <fold v-else/>
            </el-icon>
          </template>
        </el-button>
        <admin-tab/>
      </el-space>
    </el-col>
    <!-- 用户信息 -->
    <el-col :span="4">
      <el-row :gutter="10" justify="end" align="middle">
        <el-icon class="mouse-over" :size="iconSize-2" @click="doFullScreen">
          <full-screen/>
        </el-icon>
        <el-icon class="mouse-over" :size="iconSize-2" @click="clickToGo('Home')">
          <house/>
        </el-icon>
        <el-dropdown>
          <span class="mouse-over">
            <el-avatar v-if="userStore.userinfo.avatar" shape="square" :src="avatar"/>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="clickToGo('UserCenterView')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="doLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-row>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import {router} from "@/router";
import {useRoute} from "vue-router";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {loginInfo, logout} from "@/api/system/sys-login-api";
import AdminTab from "@/layout/modules/AdminTab.vue";
import {Expand, Fold, FullScreen, House} from "@element-plus/icons-vue";
import {onMounted, ref, watch} from 'vue';
import default_avatar from "@/assets/svg-source/default-avatar.svg";
import {useUserStore} from "@/stores/user.ts";
import {useMenuStore} from "@/stores/menu.ts";

const userStore = useUserStore();
const menuStore = useMenuStore();
const route = useRoute();

const iconSize = ref<number>(26);
const avatar = ref<any>(default_avatar);

onMounted(() => {
  reqCommonFeedback(loginInfo(), (data:any) => userStore.setUserInfo(data));
});

/**
 * 点击跳转
 */
const clickToGo = (name: string) => {
  router.push({name: name});
}

/**
 * 退出登录
 */
const doLogout = () => {
  reqCommonFeedback(logout(), () => {
    userStore.setUserInfo({id: '', username: '', nickname: ''});
    router.push({name: 'Login', query: {redirect: route.name ? route.name.toString() : ''}});
  });
}

/**
 * 浏览器全屏
 */
const doFullScreen = (event: { exitFullscreen: () => void; }) => {
  // 点击切换全屏模式
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    document.documentElement.requestFullscreen()
  }
}

watch(()=>userStore.userinfo, (userinfo) => {
  let avatarJpg = userinfo.avatar;
  if (avatarJpg) {
    avatar.value = `api/system/file/getAvatar?avatar=${avatarJpg}`;
  }
});
</script>

<style>
.header-row {
  height: 63px;
}

.mouse-over {
  cursor: pointer;
  padding: 0 3px;
}
</style>
