<template>
  <div class="bg">
    <!-- 综合统计 -->
    <el-row :gutter="12">
      <el-col :span="6" v-for="item in countList" :key="item.title">
        <el-card shadow="never" body-style>
          <div style="display: flex;flex-direction: column;align-items: center">
            <h3>{{ item.title }}</h3>
            <span>{{ item.count }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- cpu使用情况 -->
    <el-row style="margin-top: 1em">
      <el-col>
        <el-card shadow="never">
          <el-descriptions :title="t('dashboard.cpu')" direction="vertical" :column="4" border>
            <el-descriptions-item :label="t('dashboard.cpuCount')">
              {{ systemInfo.data.cpuCount }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.cpuSystemUsed')">
              {{ systemInfo.data.cpuSystemUsed }}%
            </el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.cpuUserUsed')">
              {{ systemInfo.data.cpuUserUsed }}%
            </el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.cpuFree')">
              {{ systemInfo.data.cpuFree }}%
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统信息 -->
    <el-row style="margin-top: 1em">
      <el-col>
        <el-card shadow="never">
          <el-descriptions :title="t('dashboard.system')" direction="vertical" :column="4" border>
            <el-descriptions-item :label="t('dashboard.os')">{{ systemInfo.data.os }}</el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.serverName')">{{ systemInfo.data.serverName }}</el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.serverIp')">{{ systemInfo.data.serverIp }}</el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.serverArchitecture')">{{ systemInfo.data.serverArchitecture }}</el-descriptions-item>

            <el-descriptions-item :label="t('dashboard.javaName')">{{ systemInfo.data.javaName }}</el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.javaVersion')">{{ systemInfo.data.javaVersion }}</el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.javaPath')">{{ systemInfo.data.javaPath }}</el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.projectPath')">{{ systemInfo.data.projectPath }}</el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.runningTime')">
              {{ unitUtil.timeCalculate(systemInfo.data.runningTime) }}
            </el-descriptions-item>

            <el-descriptions-item :label="t('dashboard.totalMemory')">
              {{ unitUtil.memoryCalculate(systemInfo.data.memoryTotalSize) }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.availableMemory')">
              {{ unitUtil.memoryCalculate(systemInfo.data.memoryAvailableSize) }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.usedMemory')">
              {{ unitUtil.memoryCalculate(systemInfo.data.memoryTotalSize - systemInfo.data.memoryAvailableSize) }}
            </el-descriptions-item>

            <el-descriptions-item :label="t('dashboard.totalDisk')">
              {{ unitUtil.memoryCalculate(systemInfo.data.diskTotalSize) }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.availableSpace')">
              {{ unitUtil.memoryCalculate(systemInfo.data.diskFreeSize) }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.usedSpace')">
              {{ unitUtil.memoryCalculate(systemInfo.data.diskTotalSize - systemInfo.data.diskFreeSize) }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.diskPath')">{{ systemInfo.data.diskPath }}</el-descriptions-item>
            <el-descriptions-item :label="t('dashboard.diskSeparator')">{{ systemInfo.data.diskSeparator }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {getSystemInfo, getCount} from "@/api/system/sys-dashboard-api";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import unitUtil from "@/utils/unit-util";
import {useI18n} from "vue-i18n";

const {t} = useI18n();

// 系统信息
const systemInfo = reactive<any>({data: {cpuCount:0, cpuSystemUsed:0, cpuUserUsed:0, cpuFree:0}});

// 表单统计
const countList = ref<any[]>([]);

onMounted(() => {
  initCount();
  initSystemInfo();
})

/**
 * 初始化系统信息
 */
const initSystemInfo = () => {
  reqCommonFeedback(getSystemInfo(), (systemModel: SystemModel) => {
    systemInfo.data = systemModel;
  });
}

/**
 * 数据统计
 */
const initCount = () => {
  reqCommonFeedback(getCount(), (data: any) => {
    countList.value = data;
  });
}
</script>

<style scoped>

</style>
