<template>
  <table-manage>
    <!-- 表格操作 -->
    <template #search>
      <el-form-item :label="t('dictionary.name')">
        <el-input :placeholder="t('dictionary.name')" v-model:model-value="searchObj.dictionaryName"/>
      </el-form-item>
      <el-form-item :label="t('dictionary.enableStatus')">
        <el-select :placeholder="t('common.selectEnableStatus')" style="width: 200px" v-model="searchObj.enableStatus">
          <el-option v-for="i in enableStatusList" :label="t(i.label)" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadTableData" :icon="Search">{{ t('common.search') }}</el-button>
        <el-button @click="resetSearchForm" :icon="Refresh">{{ t('common.reset') }}</el-button>
        <el-button @click="onExpandAll">
          <el-icon>
            <arrow-right-bold v-if="!isExpandAll"/>
            <arrow-down-bold v-else/>
          </el-icon>
          {{ isExpandAll ? t('common.collapse') : t('common.expand') }}
        </el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button type="primary" @click="onAdd" :icon="Plus">{{ t('dictionary.add') }}</el-button>
      <el-button plain type="danger" @click="onDeleteBatch" :icon="DeleteFilled">{{ t('dictionary.batchDelete') }}</el-button>
    </template>

    <!-- 表格视图 -->
    <template #default>
      <el-table v-if="isShowTable" stripe row-key="id" :data="records" v-model:default-expand-all="isExpandAll"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="80"/>
        <el-table-column prop="dictionaryName" :label="t('dictionary.labelName')" sortable show-overflow-tooltip/>
        <el-table-column prop="remark" :label="t('dictionary.remark')" show-overflow-tooltip/>
        <el-table-column prop="enableStatus" :label="t('dictionary.isEnable')">
          <template #default="scope">
            <el-tag :type="getConfirm(scope.row.enableStatus, 0)">{{ getConfirm(scope.row.enableStatus, 1) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" :label="t('dictionary.sort')" sortable/>
        <el-table-column prop="createBy" :label="t('dictionary.createBy')" show-overflow-tooltip />
        <el-table-column prop="createTime" :label="t('dictionary.createTime')" width="200" />
        <!-- 单行操作 -->
        <el-table-column fixed="right" width="200" :label="t('dictionary.operation')">
          <template #default="scope">
            <el-button size="small" @click="onEdit(scope.row)" :icon="Edit">{{ t('common.edit') }}</el-button>
            <el-button size="small" plain type="danger" @click="onRemove(scope.row)" :icon="DeleteFilled">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 编辑对话框 -->
    <template #form>
      <el-dialog v-model="dialogFormVisible" :title="editForm.id ? t('dictionary.editTitle') : t('dictionary.addTitle')">
        <el-form ref="sttFormRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="dictionaryName" :label="t('dictionary.name')">
            <el-input v-model="editForm.dictionaryName"></el-input>
          </el-form-item>
          <el-form-item prop="remark" :label="t('dictionary.remark')">
            <el-input v-model="editForm.remark"></el-input>
          </el-form-item>
          <el-form-item prop="enableStatus" :label="t('dictionary.isEnable')">
            <el-radio-group v-model="editForm.enableStatus">
              <el-radio :label="0">{{ t('common.yes') }}</el-radio>
              <el-radio :label="1">{{ t('common.no') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="sort" :label="t('dictionary.sortLabel')">
            <el-input v-model="editForm.sort" type="number"></el-input>
          </el-form-item>
          <el-form-item :label="t('dictionary.parent')">
            <el-cascader clearable v-model="editForm.parentId" :placeholder="t('common.selectNode')"
                         :props="defaultProps" :options="records" :show-all-levels="false"
                         @change="handleChange">
            </el-cascader>
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
import {nextTick, onMounted, ref, reactive} from "vue";
import {listByTree, add, deleteBatch, update} from "@/api/system/sys-dictionary-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm} from "element-plus/es";
import {ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Refresh, Search} from "@element-plus/icons-vue";
import {useI18n} from "vue-i18n";

const {t} = useI18n();

type FormInstance = InstanceType<typeof ElForm>
const sttFormRef = ref<FormInstance>();

// 级联选择框配置
const defaultProps = {
  value: 'id',
  label: 'dictionaryName',
  children: 'children',
  checkStrictly: true
}

const records = ref<any>();
const searchObj = ref<DictionaryModel>({});
const isExpandAll = ref<boolean>(true);
// 表单参数
const editForm = ref<DictionaryModel>({});
// 加载进度
const loading = ref<boolean>(true);
// 表单校验规则
const rules = reactive({
  dictionaryName: [{required: true, min: 2, max: 30, message: t('common.lengthLimit', {min: 2, max: 30}), trigger: 'blur'}],
  enableStatus: [{required: true, message: t('common.selectEnableStatus'), trigger: 'blur'}],
  remark: [{min: 2, max: 255, message: t('common.lengthLimit', {min: 2, max: 255}), trigger: 'blur'}]
});
// 是否显示外链选择按钮
const dialogFormVisible = ref<boolean>(false);
const isShowTable = ref<boolean>(true);
const multipleSelection = ref<any[]>([]);
const enableStatusList = ref<any>([
  // label 存 i18n key，渲染时翻译
  {label: 'common.enable', value: 0},
  {label: 'common.disable', value: 1}
]);

// 初始化数据
onMounted(() => {
  loadTableData();
});

const onEdit = (row: DictionaryModel): void => {
  editForm.value = row;
  dialogFormVisible.value = true;
}

const onAdd = () => {
  dialogFormVisible.value = true;
  editForm.value = {enableStatus: 1};
}

const onRemove = (row: DictionaryModel): void => {
  ElMessageBox.confirm(t('dictionary.confirmDelete'), t('common.tip'), {
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
  const param = searchObj.value;
  reqCommonFeedback(listByTree(param), (data: any) => {
    records.value = data;
    loading.value = false;
  });
}

const doUpdate = (formEl: any): void => {
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

const onExpandAll = () => {
  isShowTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    isShowTable.value = true;
  });
}

const resetSearchForm = () => {
  searchObj.value = {};
}

const handleSelectionChange = (arr: any) => {
  multipleSelection.value = arr;
}

const onDeleteBatch = () => {
  let ids: string[] = [];
  multipleSelection.value.map((item, index) => ids.push(item.id));
  ElMessageBox.confirm(t('dictionary.confirmDeleteSelected'), t('common.tip'), {
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

const getConfirm: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'success', text: t('common.yes')};
      break;
    case 1:
      obj = {color: 'info', text: t('common.no')};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
</script>

<style scoped></style>
