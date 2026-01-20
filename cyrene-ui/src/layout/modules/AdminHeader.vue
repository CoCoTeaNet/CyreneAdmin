<template>
  <el-row align="middle" class="header-container">
    <el-col :span="16" class="header-left">
      <div class="collapse-btn" @click="menuStore.setCollapseMenu">
        <el-icon :size="20">
          <expand v-if="menuStore.isCollapseMenu"/>
          <fold v-else/>
        </el-icon>
      </div>
      <admin-tab />
    </el-col>

    <el-col :span="8" class="header-right">
      <div class="action-items">
        <el-tooltip content="全屏" placement="bottom">
          <div class="action-icon" @click="doFullScreen">
            <el-icon :size="18"><full-screen/></el-icon>
          </div>
        </el-tooltip>
        
        <el-tooltip content="首页" placement="bottom">
          <div class="action-icon" @click="clickToGo('Home')">
            <el-icon :size="18"><house/></el-icon>
          </div>
        </el-tooltip>

        <el-dropdown trigger="click" class="user-dropdown">
          <div class="user-info">
            <el-avatar 
              :size="32" 
              :src="avatar" 
              class="user-avatar"
            />
            <span class="user-name">{{ userStore.userinfo.nickname || '管理员' }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="modern-dropdown">
              <el-dropdown-item @click="clickToGo('UserCenterView')">
                <el-icon><user /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item divided @click="doLogout" class="logout-item">
                <el-icon><switch-button /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import {router} from "@/router";
import {useRoute} from "vue-router";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {loginInfo, logout, userMenu} from "@/api/system/sys-login-api";
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
  reqCommonFeedback(userMenu(), (data:MenuModel[]) => menuStore.setUserMenu(data));
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
    menuStore.setUserMenu([]);
    menuStore.initTabItems();
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
    avatar.value = `${import.meta.env.VITE_API_CONTEXT_PATH}/system/user/getAvatar?avatar=${avatarJpg}`;
  }
});
</script>

<style scoped>
.header-container {
  height: 60px;
  padding: 0 15px;
  background-color: #fff;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
}

.header-right {
  justify-content: flex-end;
}

/* 侧边栏折叠按钮 */
.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin-right: 10px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.collapse-btn:hover {
  background-color: var(--el-fill-color-light);
}

/* 右侧图标样式 */
.action-items {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  color: var(--el-text-color-regular);
  transition: all 0.2s;
}

.action-icon:hover {
  background-color: var(--el-fill-color-light);
  color: var(--el-color-primary);
}

/* 用户区域样式 */
.user-info {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: var(--el-fill-color-light);
}

.user-avatar {
  border: 2px solid #fff;
  box-shadow: 0 0 4px rgba(0,0,0,0.1);
}

.user-name {
  margin-left: 10px;
  font-size: 14px;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

/* 下拉菜单美化 */
.modern-dropdown {
  padding: 8px;
  border-radius: 12px;
}

.logout-item {
  color: var(--el-color-danger) !important;
}
</style>