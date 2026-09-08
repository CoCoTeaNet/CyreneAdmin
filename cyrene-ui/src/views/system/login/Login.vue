<template>
  <el-row class="a-login">
    <div class="a-login-inner">
      <div class="a-login-left">
        <h1 class="welcome-title">{{ t('login.welcomeTitle') }}</h1>
        <p class="welcome-sub">{{ t('login.welcomeSub') }}</p>
      </div>

      <el-card class="a-login-right a-login-card" shadow="never">
        <div>
          <div class="login-title">{{ t('login.title') }}</div>
          <div class="login-subtitle">{{ t('login.subtitle') }}</div>
        </div>

        <el-form ref="loginFormRef" :model="loginForm" status-icon :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input :placeholder="t('login.username')" :prefix-icon="UserFilled" v-model="loginForm.username" autocomplete="off" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input :placeholder="t('login.password')" :prefix-icon="Lock" v-model="loginForm.password"
                      @keypress.enter="submitForm(loginFormRef)" type="password" autocomplete="off" />
          </el-form-item>

          <el-form-item prop="captcha">
            <div class="captcha-row">
              <el-input :placeholder="t('login.captcha')" :prefix-icon="Connection" @keydown.enter="submitForm(loginFormRef)"
                        v-model="loginForm.captcha" />
              <el-image class="captcha-img" @click="getVerifyCodeImage" :src="captcha" fit="cover" />
            </div>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="loginForm.rememberMe">{{ t('login.rememberMe') }}</el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button style="width: 100%" type="primary" @click="submitForm(loginFormRef)" :loading="loading">
              {{ t('login.login') }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </el-row>
</template>

<script setup lang="ts">
import {nextTick, onMounted, reactive, ref} from "vue";
import type {ElForm} from 'element-plus';
import {ElMessage} from "element-plus";
import {Connection, Lock, UserFilled} from "@element-plus/icons-vue";
import {getCaptcha, login} from "@/api/system/sys-login-api";
import {useRoute, useRouter} from "vue-router";
import 'element-plus/theme-chalk/display.css';
import {ApiResultEnum} from "@/api/ApiResultEnum";
import {getSm2} from '@/utils/smUtil';
import {useI18n} from "vue-i18n";

const {t} = useI18n();

const sm2 = getSm2();
const router = useRouter();
const route = useRoute();

type FormInstance = InstanceType<typeof ElForm>
const loginFormRef = ref<FormInstance>();

// 登录加载中
const loading = ref<boolean>(false);

// 验证码
const captcha = ref<string>('');

// 表单对象
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaId: '', // 验证码ID
  publicKey: [], // 公钥
  rememberMe: true
});

// 表单校验规则
const rules = reactive({
  username: [{required: true, min: 2, max: 16, message: t('common.lengthLimit', {min: 2, max: 16}), trigger: 'blur'}],
  password: [{required: true, min: 6, max: 30, message: t('common.lengthLimit', {min: 6, max: 30}), trigger: 'blur'}],
  captcha: [{required: true, message: t('login.captchaRequired'), trigger: 'blur'}],
});

onMounted(() => {
  getVerifyCodeImage();
});

/**
 * 获取验证码
 */
const getVerifyCodeImage = () => {
  let timestamp = new Date().getTime();
  getCaptcha(timestamp).then((res: any) => {
    if (res.code === ApiResultEnum.SUCCESS) {
      captcha.value = `data:image/jpeg;base64,${res.data.imgBase64}`;
      loginForm.captchaId = res.data.captchaId;
      loginForm.publicKey = res.data.publicKey;
    }
  });
}

/**
 * 提交登录信息
 * @param formEl FormInstance
 */
const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate((valid: any) => {
    if (valid) {
      loading.value = true;

      // see https://github.com/JuneAndGreen/sm-crypto/issues/72
      let encPassword = '04' + sm2.doEncrypt(loginForm.password, loginForm.publicKey);
      let loginParams = {password: ''};
      Object.assign(loginParams, loginForm);
      loginParams.password = encPassword;

      nextTick(() => {
        login(loginParams).then((res: any) => {
          if (res.code === ApiResultEnum.SUCCESS) {
            if (route.query.redirect) {
              router.push({name: route.query.redirect + ''});
            } else {
              router.push({name: 'Home'});
            }
          } else {
            ElMessage.error(res.message);
            getVerifyCodeImage();
          }
          loading.value = false;
        });
      });
    }
  });
}
</script>

<style scoped src="./Login.css"></style>
