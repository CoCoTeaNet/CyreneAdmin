<template>
  <table-manage>
    <template #search>
      <el-form-item :label="t('sysLog.logId')">
        <el-input :placeholder="t('sysLog.logId')" v-model="pageParam.searchObject.id"/>
      </el-form-item>
      <el-form-item :label="t('sysLog.operator')">
        <el-input :placeholder="t('sysLog.operator')" v-model="pageParam.searchObject.operator"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadTableData" :icon="Search">{{ t('common.search') }}</el-button>
        <el-button @click="onReset" :icon="Refresh">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button type="danger" @click="onBatchDelete" :icon="DeleteFilled">{{ t('dictionary.batchDelete') }}</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="id" :label="t('sysLog.logId')" width="200"/>
        <el-table-column prop="operator" :label="t('sysLog.operationPerson')" show-overflow-tooltip>
          <template #default="scope">
            <el-text>{{`${scope.row.username}@${scope.row.nickname}`}}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="apiPath" :label="t('sysLog.apiName')" width="250" show-overflow-tooltip/>
        <el-table-column prop="ipAddress" :label="t('sysLog.ipAddress')" show-overflow-tooltip/>
        <el-table-column prop="requestWay" :label="t('sysLog.requestWay')"/>
        <el-table-column prop="logStatus" :label="t('sysLog.operationStatus')">
          <template #default="scope">
            <el-tag :type="getOperationStatus(scope.row.logStatus, 0)">
              {{getOperationStatus(scope.row.logStatus, 1)}}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="logType" :label="t('sysLog.logType')">
          <template #default="scope">
            <el-tag :type="getLogType(scope.row.logType, 0)">{{getLogType(scope.row.logType, 1)}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('sysLog.operationTime')" width="200"/>

        <el-table-column fixed="right" :label="t('dictionary.operation')" width="80">
          <template #default="scope">
            <el-button size="small" type="danger" plain @click="onDelete(scope.row.id)" :icon="DeleteFilled">
              {{ t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="pageVo.total" :page-size="pageParam.pageSize" :page-sizes=[10,20,30]
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import operationLogApi from "@/api/system/sys-log-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Refresh, Search} from "@element-plus/icons-vue";
import {useI18n} from "vue-i18n";

const {t} = useI18n();

// 分页参数
const pageParam = ref<PageParam>({pageNo: 1, pageSize: 20, searchObject: {}});
// api返回的分页数据
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 20, total: 0, records: []});
// 加载进度
const loading = ref<boolean>(true);
const getLogType: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 1:
      obj = {color: 'success', text: t('sysLog.loginLog')};
      break;
    case 2:
      obj = {color: 'warning', text: t('sysLog.operationLog')};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
const getOperationStatus: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'danger', text: t('sysLog.abnormal')};
      break;
    case 1:
      obj = {color: 'success', text: t('sysLog.success')};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
const multipleSelection = ref<any[]>([]);

// 初始化数据
onMounted(() => {
  loadTableData();
});

const onReset = (): void => {
  pageParam.value.searchObject = {};
}

const onDelete = (id: string) => {
  reqSuccessFeedback(operationLogApi.deleteBatch([id]), t('common.deleteSuccess'), () => {
    loadTableData();
  });
}

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    sysLog: pageParam.value.searchObject
  };
  reqCommonFeedback(operationLogApi.listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}

const onBatchDelete = () => {
  let ids: string[] = [];
  multipleSelection.value.map((item) => ids.push(item.id));
  ElMessageBox.confirm(t('sysLog.confirmDeleteSelected'), t('common.tip'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      }
  ).then(() => {
    reqCommonFeedback(operationLogApi.deleteBatch(ids), () => {
      ElMessage({type: 'success', message: t('common.deleteSuccess')});
      loadTableData();
    });
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

const handleSelectionChange = (arr: any) => {
  multipleSelection.value = arr;
}
</script>

<style scoped></style>
