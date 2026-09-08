<template>
  <table-manage>
    <!-- 表格操作 -->
    <template #search>
      <el-form>
        <el-form-item :label="t('menu.menuName')">
          <el-input :placeholder="t('menu.menuName')" v-model:model-value="searchObj.menuName"/>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="loadTableData" :icon="Search">{{ t('common.search') }}</el-button>
      <el-button @click="resetSearchForm" :icon="RefreshRight">{{ t('common.reset') }}</el-button>
      <el-button @click="onExpandAll">
        <el-icon>
          <arrow-right-bold v-if="!isExpandAll"/>
          <arrow-down-bold v-else/>
        </el-icon>
        {{ isExpandAll ? t('common.collapse') : t('common.expand') }}
      </el-button>
    </template>

    <template #operate>
      <el-button type="primary" @click="onAdd" :icon="Plus">{{ t('menu.addMenu') }}</el-button>
    </template>

    <!-- 表格视图 -->
    <template #default>
      <el-table v-if="isShowTable" stripe row-key="id" :data="records" v-model:default-expand-all="isExpandAll">
        <el-table-column prop="iconPath" width="100" :label="t('menu.icon')">
          <template #default="scope">
            <el-icon v-if="scope.row.iconPath">
              <component :is="scope.row.iconPath"></component>
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="menuName" width="200" :label="t('menu.name')"/>
        <el-table-column width="300" prop="routerPath" :label="t('menu.routerPath')"/>
        <el-table-column prop="menuType" :label="t('menu.menuType')">
          <template #default="scope">
            <el-tag :type="getMenuType(scope.row.menuType, 0)">{{ getMenuType(scope.row.menuType, 1) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isExternalLink" :label="t('menu.isExternalLink')">
          <template #default="scope">
            <el-tag :type="getConfirm(scope.row.isExternalLink, 0)">{{ getConfirm(scope.row.isExternalLink, 1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="menuStatus" :label="t('menu.menuStatus')">
          <template #default="scope">
            <el-tag :type="getMenuStatus(scope.row.menuStatus, 0)">{{ getMenuStatus(scope.row.menuStatus, 1) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" width="100" :label="t('menu.weight')"/>
        <el-table-column prop="createTime" width="200" :label="t('menu.createTime')"/>
        <el-table-column prop="updateTime" width="200" :label="t('menu.updateTime')"/>
        <!-- 单行操作 -->
        <el-table-column fixed="right" width="200" :label="t('dictionary.operation')">
          <template #default="scope">
            <el-button size="small" @click="onEdit(scope.row)" :icon="Edit">{{ t('common.edit') }}</el-button>
            <el-button size="small" plain type="danger" @click="onRemove(scope.row)" :icon="DeleteFilled">
              {{ t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 编辑对话框 -->
    <template #form>
      <el-dialog v-model="dialogFormVisible" :title="editForm.id ? t('common.edit') + t('menu.menu') : t('common.add') + t('menu.menu')">
        <el-form ref="sttFormRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="menuName" :label="t('menu.menuName')">
            <el-input v-model="editForm.menuName"></el-input>
          </el-form-item>
          <el-form-item prop="menuType" :label="t('menu.menuType')">
            <el-radio-group v-model="editForm.menuType" @change="menuTypeChange">
              <el-radio :label="0">{{ t('menu.directory') }}</el-radio>
              <el-radio :label="1">{{ t('menu.menu') }}</el-radio>
              <el-radio :label="2">{{ t('menu.button') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="routerPath" :label="t('menu.routerPath')">
            <el-input v-model="editForm.routerPath"></el-input>
          </el-form-item>
          <el-form-item v-if="isShowExternalLink" prop="isExternalLink" :label="t('menu.isExternalLink')">
            <el-radio-group v-model="editForm.isExternalLink">
              <el-radio :label="0">{{ t('common.no') }}</el-radio>
              <el-radio :label="1">{{ t('common.yes') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="sort" :label="t('menu.sort')">
            <el-input v-model="editForm.sort" type="number"></el-input>
          </el-form-item>
          <el-form-item :label="t('menu.menuIcon')">
            <icon-selection v-model="editForm.iconPath" value=""/>
          </el-form-item>
          <el-form-item :label="t('menu.parentMenu')">
            <el-cascader clearable v-model="editForm.parentId" :placeholder="t('common.selectNode')"
                         :props="defaultProps" :options="records" :show-all-levels="false"
                         @change="handleChange">
            </el-cascader>
          </el-form-item>
          <el-form-item prop="menuStatus" :label="t('menu.menuStatus')">
            <el-radio-group v-model="editForm.menuStatus">
              <el-radio :label="0">{{ t('common.show') }}</el-radio>
              <el-radio :label="1">{{ t('common.hide') }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogFormVisible = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="doUpdate(sttFormRef)">{{ t('common.confirm') }}</el-button>
        </span>
        </template>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, reactive, ref} from "vue";
import IconSelection from "@/components/selection/IconSelection.vue";
import {listByTree, add, deleteBatch, update} from "@/api/system/sys-menu-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm} from "element-plus/es";
import {ElMessageBox} from "element-plus";
import listUtil from "@/utils/list-util";
import {DeleteFilled, Edit, Plus, RefreshRight, Search} from "@element-plus/icons-vue";
import {useI18n} from "vue-i18n";

const {t} = useI18n();

type FormInstance = InstanceType<typeof ElForm>
const sttFormRef = ref<FormInstance>();

// 级联选择框配置
const defaultProps = {
  value: 'id',
  label: 'menuName',
  children: 'children',
  checkStrictly: true
}

const records = ref<any>();
const searchObj = ref<MenuModel>({});
const isExpandAll = ref<boolean>(true);
const getMenuType: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'success', text: t('menu.directory')};
      break;
    case 1:
      obj = {color: 'info', text: t('menu.menu')};
      break;
    case 2:
      obj = {color: 'info', text: t('menu.button')};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
const getConfirm: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'info', text: t('common.no')};
      break;
    case 1:
      obj = {color: 'success', text: t('common.yes')};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
const getMenuStatus: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'success', text: t('common.show')};
      break;
    case 1:
      obj = {color: 'warning', text: t('common.hide')};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
// 表单参数
const editForm = ref<MenuModel>({});
// 加载进度
const loading = ref<boolean>(true);
// 表单校验规则
const rules = reactive({
  menuName: [{required: true, min: 2, max: 30, message: t('common.lengthLimit', {min: 2, max: 30}), trigger: 'blur'}],
  menuType: [{required: true, message: t('menu.errMenuType'), trigger: 'blur'}],
  menuStatus: [{required: true, message: t('menu.errMenuStatus'), trigger: 'blur'}],
  routerPath: [{required: true, min: 2, max: 255, message: t('common.lengthLimit', {min: 2, max: 255}), trigger: 'blur'}],
  isExternalLink: [{required: true, message: t('menu.errLinkType'), trigger: 'blur'}],
});
// 是否显示外链选择按钮
const isShowExternalLink = ref<boolean>(true);
const dialogFormVisible = ref<boolean>(false);
const isShowTable = ref<boolean>(true);

// 初始化数据
onMounted(() => {
  loadTableData();
});

const onEdit = (row: MenuModel): void => {
  editForm.value = row;
  dialogFormVisible.value = true;
}

const onAdd = () => {
  dialogFormVisible.value = true;
  editForm.value = {menuType: 1, menuStatus: 0, isExternalLink: 0};
}

const onRemove = (row: MenuModel): void => {
  ElMessageBox.confirm(t('menu.confirmDelete'), t('common.tip'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      }
  ).then(() => {
    reqSuccessFeedback(deleteBatch([row.id]), t('common.deleteSuccess'), () => {
      loadTableData();
    });
  });
}

const loadTableData = (): void => {
  if (!loading.value) loading.value = true;
  let param = {isMenu: 1, menuName: searchObj.value.menuName};
  reqCommonFeedback(listByTree(param), (data: any) => {
    listUtil.treeMap(data, (item: { disabled: boolean; menuType: number; }) => item.disabled = (item.menuType != 0));
    records.value = data;
    loading.value = false;
  });
}

const doUpdate = (formEl: any): void => {
  editForm.value.isMenu = 1;
  formEl.validate((valid: any) => {
    if (valid) {
      if (!editForm.value.id) {
        reqSuccessFeedback(add(editForm.value), t('common.addSuccess'), () => {
          loadTableData();
          dialogFormVisible.value = false;
        });
      } else {
        reqSuccessFeedback(update(editForm.value), t('common.updateSuccess'), () => {
          loadTableData();
          dialogFormVisible.value = false;
        });
      }
    }
  });
}

const handleChange = (data: any) => {
  if (!data) {
    editForm.value.parentId = '0';
    return;
  }
  editForm.value.parentId = data[data.length - 1] ? data[data.length - 1] : '0';
}

const menuTypeChange = (value: number) => {
  isShowExternalLink.value = value === 1;
}

const onExpandAll = () => {
  isShowTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    isShowTable.value = true;
  });
}

const resetSearchForm = () => {
  searchObj.value.menuName = '';
}
</script>

<style scoped></style>
