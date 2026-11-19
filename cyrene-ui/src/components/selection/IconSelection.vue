<template>
  <el-select :model-value="modelValue" filterable reserve-keyword :placeholder="t('common.inputKeyword')" :loading="loading"
             @update:model-value="onUpdate">
    <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value">
      <div style="display: flex;align-items: center;">
        <el-space>
          <el-icon><component :is="item.value"></component></el-icon>
          <span>{{item.label}}</span>
        </el-space>
      </div>
    </el-option>
  </el-select>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import * as ElIcons from '@element-plus/icons-vue'
import {useI18n} from 'vue-i18n'

const {t} = useI18n()

interface ListItem {
  value: string
  label: string
}

const list = ref<ListItem[]>([])
const options = ref<ListItem[]>([])
const loading = ref(false)

onMounted(()=>{
  loadIcons();
});

withDefaults(defineProps<{
  modelValue?: string
}>(), {modelValue: ''});

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>();

const onUpdate = (value: string) => {
  emit('update:modelValue', value);
}

// 加载element图标
let l: ListItem[] = [];
for (const name in ElIcons) {
  l.push({value: name, label: name});
}
list.value = l;

const loadIcons = () => {
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
    options.value = list.value;
  }, 200);
}

</script>
