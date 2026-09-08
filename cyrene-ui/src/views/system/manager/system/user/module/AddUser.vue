<template>
  <el-dialog :model-value="show"
             :title="editType === 'update' ? t('addUser.updateTitle') : t('addUser.addTitle')"
             width="50%"
             @close="onCancel">
    <el-form :model="dataForm" ref="sstFormRef" label-width="100px" :rules="rules" style="max-height: 600px">
      <el-form-item prop="username" :label="t('addUser.accountName')">
        <el-input :placeholder="t('addUser.inputAccount')" v-model="dataForm.username"></el-input>
      </el-form-item>
      <el-form-item prop="nickname" :label="t('addUser.nickname')">
        <el-input :placeholder="t('addUser.inputNickname')" v-model="dataForm.nickname"></el-input>
      </el-form-item>
      <el-form-item prop="password" :label="t('addUser.password')">
        <el-input :placeholder="t('addUser.passwordPlaceholder')" :prefix-icon="Lock" v-model="dataForm.password" type="password"></el-input>
      </el-form-item>
      <el-form-item prop="email" :label="t('addUser.email')">
        <el-input placeholder="example@xx.com" v-model="dataForm.email"></el-input>
      </el-form-item>
      <el-form-item prop="roleIds" :label="t('addUser.role')">
        <el-select v-model="dataForm.roleIds" :placeholder="t('common.selectRole')" :multiple="true">
          <el-option v-for="item in roleOptions" :key="item.id" :label="item.roleName" :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item prop="sort" :label="t('addUser.sex')">
        <el-radio-group v-model="dataForm.sex">
          <el-radio :label="0">{{ t('user.sexSecret') }}</el-radio>
          <el-radio :label="1">{{ t('user.sexMale') }}</el-radio>
          <el-radio :label="2">{{ t('user.sexFemale') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="sort" :label="t('addUser.status')">
        <el-radio-group v-model="dataForm.accountStatus">
          <el-radio :label="0">{{ t('user.statusDisabled') }}</el-radio>
          <el-radio :label="1">{{ t('user.statusNormal') }}</el-radio>
          <el-radio :label="2">{{ t('user.statusFrozen') }}</el-radio>
          <el-radio :label="3">{{ t('user.statusBanned') }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="onCancel">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="onConfirm(sstFormRef)">{{ t('common.confirm') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {onMounted, reactive, ref, watch} from 'vue';
import {ElInput, FormInstance} from 'element-plus';
import {Lock} from "@element-plus/icons-vue";
import {add, update} from '@/api/system/sys-user-api';
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import roleApi from "@/api/system/sys-role-api";
import {useI18n} from "vue-i18n";

const {t} = useI18n();

const props = withDefaults(defineProps<{
  show?: boolean,
  editType: string,
  user: UserModel
}>(), {
  show: false
});

const dataForm = ref<UserModel>();
const sstFormRef = ref<FormInstance>();
const roleOptions = ref<RoleModel[]>([]);
const rules = reactive({
  username: [{required: true, min: 2, max: 30, message: t('common.lengthLimit', {min: 2, max: 30}), trigger: 'blur'}],
  nickname: [{required: true, min: 2, max: 30, message: t('common.lengthLimit', {min: 2, max: 30}), trigger: 'blur'}],
  roleIds: [{required: true, message: t('addUser.roleRequired'), trigger: 'blur'}]
});

watch(() => props.show, (b: boolean) => {
  if (b) {
    dataForm.value = props.editType === 'update' ? props.user : {sex: 0, accountStatus: 1};
    loadRoles();
  }
});

const emit = defineEmits(['update:show', 'onConfirm']);

const loadRoles = () => {
  let param: any = {pageNo: 1, pageSize: 1000, sysRole: {id: ''}}
  reqCommonFeedback(roleApi.listByPage(param), (data: any) => {
    roleOptions.value = data.records;
  });
}

const onCancel = () => emit('update:show', false);

const onConfirm = (formEl: FormInstance) => {
  formEl.validate((valid: boolean) => {
    if (valid) {
      if (props.editType === 'create') {
        reqSuccessFeedback(add(dataForm.value), t('common.addSuccess'),() => {
          emit('update:show', false);
          emit('onConfirm');
        });
      } else if (props.editType === 'update') {
        reqSuccessFeedback(update(dataForm.value), t('common.updateSuccess'),() => {
          emit('update:show', false);
          emit('onConfirm');
        });
      }
    }
  });
}
</script>
