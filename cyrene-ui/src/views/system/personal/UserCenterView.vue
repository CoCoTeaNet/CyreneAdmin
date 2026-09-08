<template>
  <el-space style="width: 100%" direction="vertical" alignment="stretch">
    <el-card shadow="never">
      <el-descriptions :title="t('userCenter.personalInfo')" :column="3" border>
        <el-descriptions-item>
          <template #label>
            <div>
              <el-icon>
                <user/>
              </el-icon>
              {{ t('userCenter.accountName') }}
            </div>
          </template>
          {{ detailUser.username }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div>
              <el-icon>
                <user/>
              </el-icon>
              {{ t('userCenter.accountRole') }}
            </div>
          </template>

          <el-space>
            <el-tag v-for="role in detailUser.roleList" :key="role.id" type="primary">
              {{ role.roleName }}
            </el-tag>
          </el-space>

        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              <el-icon>
                <tickets/>
              </el-icon>
              {{ t('userCenter.nickname') }}
            </div>
          </template>
          {{ detailUser.nickname }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              <el-icon>
                <tickets/>
              </el-icon>
              {{ t('userCenter.sexLabel') }}
            </div>
          </template>
          {{ getSex(detailUser.sex) }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div>
              <el-icon>
                <iphone/>
              </el-icon>
              {{ t('userCenter.mobile') }}
            </div>
          </template>
          {{ detailUser.mobilePhone ? detailUser.mobilePhone : '...' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div>
              <el-icon>
                <location/>
              </el-icon>
              {{ t('userCenter.email') }}
            </div>
          </template>
          {{ detailUser.email ? detailUser.email : '...' }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              <el-icon>
                <office-building/>
              </el-icon>
              {{ t('userCenter.lastLoginIp') }}
            </div>
          </template>
          {{ detailUser.lastLoginIp }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template #label>
            <div class="cell-item">
              <el-icon>
                <office-building/>
              </el-icon>
              {{ t('userCenter.lastLoginTime') }}
            </div>
          </template>
          {{ detailUser.lastLoginTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 用户表单 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ t('userCenter.updateProfile') }}</span>
        </div>
      </template>

      <!--基本信息-->
      <el-divider>{{ t('userCenter.basicInfo') }}</el-divider>
      <el-form ref="ucvFormRef" label-width="120px" label-position="right" :rules="rules" :model="editForm">
        <el-form-item prop="avatar" :label="t('userCenter.changeAvatar')" :auto-upload="false" list-type="picture-card">
          <el-upload ref="upload"
                     :action="uploadUrl"
                     list-type="picture-card"
                     drag
                     :auto-upload="false"
                     :limit="1"
                     :file-list="fileList"
                     :on-exceed="handleExceed"
                     :on-success="handleAvatarSuccess"
                     :before-upload="beforeAvatarUpload">
            <el-icon>
              <Plus/>
            </el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item prop="nickname" :label="t('userCenter.nickname')">
          <el-input v-model="editForm.nickname"></el-input>
        </el-form-item>
        <el-form-item prop="email" :label="t('userCenter.emailLabel')">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
        <el-form-item prop="mobilePhone" :label="t('userCenter.mobile')">
          <el-input v-model="editForm.mobilePhone"></el-input>
        </el-form-item>
        <el-form-item :label="t('userCenter.sexLabel')">
          <el-radio-group v-model="editForm.sex">
            <el-radio :label="0">{{ t('user.sexSecret') }}</el-radio>
            <el-radio :label="1">{{ t('user.sexMale') }}</el-radio>
            <el-radio :label="2">{{ t('user.sexFemale') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleUploadAvatar">{{ t('userCenter.uploadAvatar') }}</el-button>
          <el-button type="primary" @click="submitForm(ucvFormRef)">{{ t('userCenter.updateInfo') }}</el-button>
        </el-form-item>
      </el-form>

      <!--修改密码-->
      <el-divider>{{ t('userCenter.modifyPassword') }}</el-divider>
      <el-form
        style="width: 50%;"
        ref="mpFormRef" label-width="120px" label-position="right" :rules="mpRules" :model="mpFormObj">
        <el-form-item prop="oldPassword" :label="t('userCenter.oldPassword')">
          <el-input :prefix-icon="Lock" v-model="mpFormObj.oldPassword" type="password"
                    autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="newPassword" :label="t('userCenter.newPassword')">
          <el-input :prefix-icon="Lock" v-model="mpFormObj.newPassword" type="password"
                    autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="newPasswordSecond" :label="t('userCenter.repeatNewPassword')">
          <el-input :prefix-icon="Lock" v-model="mpFormObj.newPasswordSecond" type="password"
                    autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onModifyPassword(mpFormRef)">{{ t('userCenter.modifyPasswordBtn') }}</el-button>
        </el-form-item>
      </el-form>

    </el-card>
  </el-space>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import type {FormInstance, UploadRawFile, UploadUserFile, UploadInstance, UploadProps} from 'element-plus';
import {genFileId} from 'element-plus';
import {Lock} from "@element-plus/icons-vue";
import {getDetail, updateByUser, doModifyPassword} from "@/api/system/sys-user-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {RULE_MOBILE, RULE_EMAIL} from "@/utils/rules-util";
import {ElMessage} from "element-plus";
import {logout} from "@/api/system/sys-login-api";
import {useUserStore} from "@/stores/user.ts";
import {useI18n} from "vue-i18n";

const {t} = useI18n();
const userStore = useUserStore();
const upload = ref<UploadInstance>();
const fileList = ref<UploadUserFile[]>([]);
const uploadUrl = `${import.meta.env.VITE_API_CONTEXT_PATH}/system/user/avatar/upload`;

const validatePhone = (rule: any, value: any, callback: any) => {
  if (!RULE_MOBILE.test(value)) {
    callback(new Error(t('userCenter.errPhone')));
  } else {
    callback();
  }
}

const validateEmail = (rule: any, value: any, callback: any) => {
  if (!RULE_EMAIL.test(value)) {
    callback(new Error(t('userCenter.errEmail')));
  } else {
    callback();
  }
}

const ucvFormRef = ref<FormInstance>();
const mpFormRef = ref<FormInstance>();
const editForm = ref<any>({});
const mpFormObj = ref<any>({});
const detailUser = ref<any>({});
// 表单校验规则
const rules = reactive({
  username: [{min: 2, max: 30, message: t('common.lengthLimit', {min: 2, max: 30}), trigger: 'blur'}],
  mobilePhone: [{validator: validatePhone, trigger: 'blur'}],
  email: [{validator: validateEmail, trigger: 'blur'}],
  nickname: [{min: 2, max: 30, message: t('common.lengthLimit', {min: 2, max: 30}), trigger: 'blur'}]
});
// 密码修改校验规则
const mpRules = reactive({
  oldPassword: [{min: 6, max: 32, message: t('common.lengthLimit', {min: 6, max: 32}), trigger: 'blur'}],
  newPassword: [{min: 6, max: 32, message: t('common.lengthLimit', {min: 6, max: 32}), trigger: 'blur'}],
  newPasswordSecond: [{min: 6, max: 32, message: t('common.lengthLimit', {min: 6, max: 32}), trigger: 'blur'}]
});
const getSex = (sex: number) => {
  switch (sex) {
    case 0:
      return t('user.sexSecret');
    case 1:
      return t('user.sexMale');
    case 2:
      return t('user.sexFemale');
  }
}

onMounted(() => {
  initUserDetail();
});

/**
 * 获取用户详细
 */
const initUserDetail = () => {
  reqCommonFeedback(getDetail(), (data: any) => {
    editForm.value = data;
    detailUser.value = Object.assign(detailUser.value, data);
    userStore.updateUserInfo(detailUser.value);
    if (data) {
      let arr = data.avatar.split('/');
      fileList.value.push({
        name: arr[arr.length - 1],
        url: `${import.meta.env.VITE_API_CONTEXT_PATH}/system/user/getAvatar?avatar=${data.avatar}`
      });
    }
  });
}

/**
 * 基本信息表单提交
 * @param ucvFormRef
 */
const submitForm = (ucvFormRef: any) => {
  ucvFormRef.validate((valid: any) => {
    if (valid) {
      reqSuccessFeedback(updateByUser(editForm.value), t('common.updateSuccess'), () => {
        initUserDetail();
      });
    }
  });
}

/**
 * 修改密码表单提交
 * @param mpFormRef
 */
const onModifyPassword = (mpFormRef: any) => {
  mpFormRef.validate((valid: any) => {
    if (valid) {
      if (mpFormObj.value.newPassword !== mpFormObj.value.newPasswordSecond) {
        ElMessage.warning(t('userCenter.pwdNotMatch'));
        return;
      }
      let param = {
        oldPassword: mpFormObj.value.oldPassword,
        newPassword: mpFormObj.value.newPassword
      };
      reqSuccessFeedback(doModifyPassword(param), t('userCenter.updateAndRelogin'), () => {
        logout().then(res => {
          setTimeout(() => {
            window.location.reload();
          }, 2000);
        });
      });
    }
  });
}

/**
 * 头像上传成功
 */
const handleAvatarSuccess = (resp: any) => {
  editForm.value.avatar = resp.data;
}

/**
 * 头像上传前校验
 */
const beforeAvatarUpload = (rawFile: UploadRawFile) => {
  if (rawFile.type === 'image/jpeg' || rawFile.type === 'image/png' || rawFile.type === 'image/jpg') {
    return true;
  } else {
    ElMessage.error(t('userCenter.unsupportedImage'));
    return false;
  }
}

const handleExceed: UploadProps['onExceed'] = (files) => {
  upload.value!.clearFiles();
  const file = files[0] as UploadRawFile;
  file.uid = genFileId();
  upload.value!.handleStart(file);
}

const handleUploadAvatar = () => {
  upload.value!.submit();
}
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>
