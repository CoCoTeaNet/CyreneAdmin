<template>
  <div style="text-align: center;padding-top: 6em">
    <img src="@/assets/svg-source/404.svg" alt=""/>
    <div>
      <p>{{ t('notFound.message') }}</p>
      <el-button type="primary" :icon="Back" @click="goHistory">{{ t('notFound.back') }}</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {router} from "@/router";
import {Back} from "@element-plus/icons-vue";
import {useI18n} from "vue-i18n";

const {t} = useI18n();

/**
 * 返回上一页
 */
const goHistory = () => {
  router.go(-1);
}
</script>

<style scoped></style>
