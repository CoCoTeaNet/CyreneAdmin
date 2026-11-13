<template>
  <div class="bg" v-loading="loading">
    <!-- 工具栏 -->
    <div class="dashboard-toolbar">
      <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadData">
        {{ t('dashboard.refresh') }}
      </el-button>
      <el-switch v-model="autoRefresh" :active-text="t('dashboard.autoRefresh')" />
      <span v-if="lastUpdated" class="last-updated">{{ t('dashboard.lastUpdated') }} {{ lastUpdated }}</span>
      <el-button v-if="loadError" type="danger" plain size="small" @click="loadData">
        {{ t('dashboard.retry') }}
      </el-button>
    </div>
    <el-alert v-if="loadError" :title="t('dashboard.loadFailed')" type="error" :closable="false" show-icon
              class="dashboard-alert" />

    <!-- 综合统计 -->
    <el-row :gutter="12">
      <el-col :xs="12" :sm="12" :md="6" v-for="item in countList" :key="item.title" class="overview-col">
        <el-card shadow="never">
          <div class="overview-card">
            <h3>{{ t('dashboard.overview.' + item.title) }}</h3>
            <span class="overview-count">{{ item.count }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <template v-if="systemInfo">
      <!-- cpu使用情况 -->
      <el-row class="dashboard-section">
        <el-col>
          <el-card shadow="never">
            <el-descriptions :title="t('dashboard.cpu')" direction="vertical" :column="4" border>
              <el-descriptions-item :label="t('dashboard.cpuCount')">
                {{ systemInfo.cpuCount }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.cpuSystemUsed')">
                {{ systemInfo.cpuSystemUsed }}%
              </el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.cpuUserUsed')">
                {{ systemInfo.cpuUserUsed }}%
              </el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.cpuFree')">
                {{ systemInfo.cpuFree }}%
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <!-- 系统信息 -->
      <el-row class="dashboard-section">
        <el-col>
          <el-card shadow="never">
            <el-descriptions :title="t('dashboard.system')" direction="vertical" :column="4" border>
              <el-descriptions-item :label="t('dashboard.os')">{{ systemInfo.os }}</el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.serverName')">{{ systemInfo.serverName }}</el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.serverIp')">{{ systemInfo.serverIp }}</el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.serverArchitecture')">{{ systemInfo.serverArchitecture }}</el-descriptions-item>

              <el-descriptions-item :label="t('dashboard.javaName')">{{ systemInfo.javaName }}</el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.javaVersion')">{{ systemInfo.javaVersion }}</el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.javaPath')">{{ systemInfo.javaPath }}</el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.projectPath')">{{ systemInfo.projectPath }}</el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.runningTime')">
                {{ unitUtil.timeCalculate(systemInfo.runningTime) }}
              </el-descriptions-item>

              <el-descriptions-item :label="t('dashboard.totalMemory')">
                {{ unitUtil.memoryCalculate(systemInfo.memoryTotalSize) }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.availableMemory')">
                {{ unitUtil.memoryCalculate(systemInfo.memoryAvailableSize) }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.usedMemory')">
                {{ unitUtil.memoryCalculate(systemInfo.memoryTotalSize - systemInfo.memoryAvailableSize) }}
              </el-descriptions-item>

              <el-descriptions-item :label="t('dashboard.totalDisk')">
                {{ unitUtil.memoryCalculate(systemInfo.diskTotalSize) }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.availableSpace')">
                {{ unitUtil.memoryCalculate(systemInfo.diskFreeSize) }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.usedSpace')">
                {{ unitUtil.memoryCalculate(systemInfo.diskTotalSize - systemInfo.diskFreeSize) }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('dashboard.diskPath')">{{ systemInfo.diskPath }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>
    </template>
    <el-empty v-else-if="!loading" :description="t('dashboard.loadFailed')" />
  </div>
</template>

<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref, watch} from "vue";
import {Refresh} from "@element-plus/icons-vue";
import {getSystemInfo, getCount} from "@/api/system/sys-dashboard-api";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {ApiResultEnum} from "@/api/ApiResultEnum";
import unitUtil from "@/utils/unit-util";
import {useI18n} from "vue-i18n";

const {t} = useI18n();

const systemInfo = ref<SystemModel | null>(null);
const countList = ref<SysOverviewModel[]>([]);
const loading = ref(false);
const loadError = ref(false);
const lastUpdated = ref('');
const autoRefresh = ref(false);

const REFRESH_INTERVAL = 10000;
let refreshTimer: number | undefined;

const loadData = () => {
  loading.value = true;
  loadError.value = false;
  const countPromise = getCount();
  const infoPromise = getSystemInfo();
  reqCommonFeedback(countPromise, (data: SysOverviewModel[]) => {
    countList.value = data;
  });
  reqCommonFeedback(infoPromise, (data: SystemModel) => {
    systemInfo.value = data;
  });
  Promise.allSettled([countPromise, infoPromise]).then((results) => {
    loading.value = false;
    const failed = results.some(r => r.status === 'rejected'
        || (r.status === 'fulfilled' && r.value.code !== ApiResultEnum.SUCCESS));
    loadError.value = failed;
    if (!failed) {
      lastUpdated.value = new Date().toLocaleTimeString();
    }
  });
}

const stopTimer = () => {
  if (refreshTimer !== undefined) {
    window.clearInterval(refreshTimer);
    refreshTimer = undefined;
  }
}

const startTimer = () => {
  stopTimer();
  refreshTimer = window.setInterval(() => {
    if (!document.hidden) {
      loadData();
    }
  }, REFRESH_INTERVAL);
}

watch(autoRefresh, (on) => {
  if (on) {
    loadData();
    startTimer();
  } else {
    stopTimer();
  }
});

const onVisibility = () => {
  if (autoRefresh.value && !document.hidden) {
    loadData();
  }
}

onMounted(() => {
  loadData();
  document.addEventListener('visibilitychange', onVisibility);
});

onBeforeUnmount(() => {
  stopTimer();
  document.removeEventListener('visibilitychange', onVisibility);
});
</script>

<style scoped>
.dashboard-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 1em;
}

.last-updated {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.dashboard-alert {
  margin-bottom: 1em;
}

.overview-col {
  margin-bottom: 12px;
}

.overview-card {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.overview-count {
  font-size: 24px;
  font-weight: 600;
  margin-top: 4px;
}

.dashboard-section {
  margin-top: 1em;
}
</style>