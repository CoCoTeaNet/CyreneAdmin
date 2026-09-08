<template>
  <table-manage>
    <template #search>
      <el-form-item :label="t('user.username')">
        <el-input :placeholder="t('user.placeholderUsername')" v-model="pageParam.searchObject.username"/>
      </el-form-item>
      <el-form-item :label="t('user.nickname')">
        <el-input :placeholder="t('user.placeholderNickname')" v-model="pageParam.searchObject.nickname"/>
      </el-form-item>
      <el-form-item :label="t('user.emailLabel')">
        <el-input :placeholder="t('user.placeholderEmail')" v-model="pageParam.searchObject.email"/>
      </el-form-item>
      <el-form-item :label="t('user.sex')">
        <el-select :placeholder="t('user.placeholderSex')" style="width: 200px" v-model="pageParam.searchObject.sex">
          <el-option v-for="i in sexList" :label="t(i.label)" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item :label="t('user.accountStatus')">
        <el-select :placeholder="t('user.placeholderStatus')" style="width: 200px" v-model="pageParam.searchObject.accountStatus">
          <el-option v-for="i in accountStatusList" :label="t(i.label)" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" @click="loadTableData">{{ t('common.search') }}</el-button>
        <el-button :icon="RefreshRight" @click="onResetSearchForm">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button :icon="Plus" type="primary" @click="onCreate">{{ t('user.addUser') }}</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">{{ t('user.batchDelete') }}</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="username" width="200" :label="t('user.account')"/>
        <el-table-column prop="nickname" width="200" :label="t('user.nick')"/>
        <el-table-column prop="roleList" width="220" :label="t('user.role')">
          <template #default="scope">
            <span style="display: flex;flex-wrap: wrap;">
              <el-tag v-for="(role, index) in scope.row.roleList" :key="index" style="margin-right: 0.5rem">
                {{role.roleName}}
              </el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sex" :label="t('user.sex')">
          <template #default="scope">
            <el-tag :type="getSex(scope.row.sex, 0)">
              {{ getSex(scope.row.sex, 1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accountStatus" :label="t('user.accountStatus')">
          <template #default="scope">
            <el-tag :type="getAccountStatus(scope.row.accountStatus, 0)">
              {{ getAccountStatus(scope.row.accountStatus, 1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" width="200" :label="t('user.placeholderEmail')"/>
        <el-table-column prop="lastLoginIp" width="200" :label="t('user.lastLoginIp')"/>
        <el-table-column prop="lastLoginTime" width="200" :label="t('user.lastLoginTime')"/>
        <el-table-column fixed="right" :label="t('user.operation')" width="180">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">{{ t('common.edit') }}</el-button>
            <el-button :icon="DeleteFilled" size="small" type="danger" plain @click="onDelete(scope.row.id)">
              {{ t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="pageVo.total" :page-size="pageParam.pageSize" :page-sizes=[5,10,15]
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>

    <template #form>
      <add-user v-model:show="formShow" :user="editUser" :edit-type="editType" @onConfirm="loadTableData"/>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, ref} from "vue";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {listByPage, deleteBatch} from "@/api/system/sys-user-api";
import TableManage from "@/components/container/TableManage.vue";
import AddUser from "@/views/system/manager/system/user/module/AddUser.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Plus, Search, RefreshRight, Edit} from "@element-plus/icons-vue";
import {useI18n} from "vue-i18n";

const {t} = useI18n();

const formShow = ref<boolean>(false);
const editType = ref<string>("create");
const editUser = ref<UserModel>();
const multipleSelection = ref<any[]>([]);
// 分页参数
const pageParam = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {}});
// api返回的分页数据
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 15, total: 0, records: []});
// 加载进度
const loading = ref<boolean>(true);
const accountStatusList = ref<any>([
  {label: 'user.statusDisabled', value: 0},
  {label: 'user.statusNormal', value: 1},
  {label: 'user.statusFrozen', value: 2},
  {label: 'user.statusBanned', value: 3}
]);
const sexList = ref<any>([
  {label: 'user.sexSecret', value: 0},
  {label: 'user.sexMale', value: 1},
  {label: 'user.sexFemale', value: 2}
]);

// 初始化数据
onMounted(() => {
  loadTableData();
});

const getAccountStatus: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'warning', text: t('user.statusDisabled')};
      break;
    case 1:
      obj = {color: 'success', text: t('user.statusNormal')};
      break;
    case 2:
      obj = {color: 'info', text: t('user.statusFrozen')};
      break;
    case 3:
      obj = {color: 'danger', text: t('user.statusBanned')};
      break;
  }
  return type === 0 ? obj.color : obj.text;
}

const getSex: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'info', text: t('user.sexSecret')};
      break;
    case 1:
      obj = {color: 'primary', text: t('user.sexMale')};
      break;
    case 2:
      obj = {color: 'success', text: t('user.sexFemale')};
      break;
  }
  return type === 0 ? obj.color : obj.text;
}

const onEdit = (row: UserModel): void => {
  formShow.value = true;
  let roleIds:any = [];
  row.roleList?.map(item => roleIds.push(item.roleId));
  row.roleIds = roleIds;
  editUser.value = row;
  editType.value = 'update';
}

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    sysUser: pageParam.value.searchObject
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}

const onPageChange = (currentPage: number) => {
  pageParam.value.pageNo = currentPage;
  nextTick(() => loadTableData());
}

const onSizeChange = (size: number) => {
  pageParam.value.pageSize = size;
  nextTick(() => loadTableData());
}

const onResetSearchForm = () => {
  pageParam.value.searchObject = {};
}

const onCreate = () => {
  formShow.value = true;
  editType.value = 'create';
}

const onDelete = (id: string) => {
  ElMessageBox.confirm(t('user.confirmDelete'), t('common.tip'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      }
  ).then(() => {
    reqCommonFeedback(deleteBatch([id]), () => {
      ElMessage({type: 'success', message: t('common.deleteSuccess')});
      loadTableData();
    });
  });
}

const onDeleteBatch = () => {
  let ids: string[] = [];
  multipleSelection.value.map((item) => ids.push(item.id));
  ElMessageBox.confirm(t('user.confirmDeleteSelected'), t('common.tip'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      }
  ).then(() => {
    reqCommonFeedback(deleteBatch(ids), () => {
      ElMessage({type: 'success', message: t('common.deleteSuccess')});
      loadTableData();
    });
  });
}

const handleSelectionChange = (arr: any) => {
  multipleSelection.value = arr;
}
</script>

<style scoped></style>