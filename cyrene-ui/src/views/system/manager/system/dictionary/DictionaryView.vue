<template>
  <div class="dm-page">
    <!-- 左侧：字典树 -->
    <section class="dm-panel dm-tree-panel" v-loading="loading">
      <header class="dm-panel-header">
        <div class="dm-panel-title">
          <el-icon class="dm-title-icon"><Collection/></el-icon>
          <span>{{ t('dictionary.treeTitle') }}</span>
          <span class="dm-count-badge">{{ totalCount }}</span>
        </div>
        <div class="dm-panel-tools">
          <el-tooltip :content="isExpandAll ? t('common.collapse') : t('common.expand')" placement="top">
            <el-button text :icon="isExpandAll ? ArrowDownBold : ArrowRightBold" @click="onToggleExpand"/>
          </el-tooltip>
          <el-tooltip :content="t('dictionary.refresh')" placement="top">
            <el-button text :icon="Refresh" @click="loadTableData"/>
          </el-tooltip>
        </div>
      </header>

      <div class="dm-filter">
        <el-input v-model="keyword" :placeholder="t('dictionary.searchPlaceholder')" :prefix-icon="Search" clearable/>
        <div class="dm-filter-row">
          <el-select v-model="statusFilter" :placeholder="t('dictionary.enableStatus')" clearable class="dm-status-select">
            <el-option v-for="i in enableStatusList" :key="i.value" :label="t(i.label)" :value="i.value"/>
          </el-select>
          <el-button text type="primary" :icon="RefreshLeft" :disabled="!hasFilter" @click="resetFilter">
            {{ t('common.reset') }}
          </el-button>
        </div>
      </div>

      <div class="dm-tree-scroll">
        <el-tree
          v-if="hasData"
          ref="treeRef"
          class="dm-tree"
          node-key="id"
          :data="treeData"
          :props="treeProps"
          :expand-on-click-node="false"
          :default-expanded-keys="expandedKeys"
          :current-node-key="selectedId"
          highlight-current
          @node-click="onNodeClick"
          @node-expand="onNodeExpand"
          @node-collapse="onNodeCollapse"
        >
          <template #default="{ data }">
            <span class="dm-tree-node" :class="{ 'is-disabled': !data.isRoot && data.enableStatus === 0 }">
              <el-icon v-if="data.isRoot" class="dm-node-root-icon"><FolderOpened/></el-icon>
              <span v-else class="dm-status-dot" :class="data.enableStatus === 1 ? 'is-on' : 'is-off'"></span>
              <span class="dm-node-label">{{ data.dictionaryName }}</span>
              <span v-if="data.children && data.children.length" class="dm-node-count">{{ data.children.length }}</span>
            </span>
          </template>
        </el-tree>
        <div v-if="hasData && hasFilter && !filteredRecords.length" class="dm-no-match">
          {{ t('dictionary.noMatch') }}
        </div>
        <el-empty v-if="!hasData" :description="t('dictionary.emptyTree')" :image-size="90">
          <el-button type="primary" :icon="Plus" @click="onAdd">{{ t('dictionary.add') }}</el-button>
        </el-empty>
      </div>

      <footer class="dm-tree-footer">
        <span class="dm-stat"><i class="dm-status-dot is-on"></i>{{ t('common.enable') }} {{ enabledCount }}</span>
        <span class="dm-stat"><i class="dm-status-dot is-off"></i>{{ t('common.disable') }} {{ disabledCount }}</span>
      </footer>
    </section>

    <!-- 右侧：选中节点的子项 -->
    <section class="dm-panel dm-detail-panel">
      <transition name="dm-swap" mode="out-in">
        <div class="dm-detail-inner" :key="selectedNode.id">
          <header class="dm-detail-header">
            <nav class="dm-breadcrumb">
              <template v-for="(crumb, idx) in breadcrumb" :key="crumb.id">
                <span v-if="idx > 0" class="dm-breadcrumb-sep">/</span>
                <a
                  class="dm-breadcrumb-item"
                  :class="{ 'is-active': idx === breadcrumb.length - 1 }"
                  @click="onSelectNode(crumb.id!, true)"
                >{{ crumb.dictionaryName }}</a>
              </template>
            </nav>
            <div class="dm-detail-title-row">
              <div class="dm-detail-title">
                <h2 :title="selectedNode.dictionaryName">{{ selectedNode.dictionaryName }}</h2>
                <el-tag v-if="!selectedNode.isRoot" size="small" :type="selectedNode.enableStatus === 1 ? 'success' : 'info'">
                  {{ selectedNode.enableStatus === 1 ? t('common.enable') : t('common.disable') }}
                </el-tag>
                <span class="dm-child-count">{{ t('dictionary.childCount', {count: childRows.length}) }}</span>
              </div>
              <div class="dm-detail-actions">
                <el-button type="primary" :icon="Plus" @click="onAdd">
                  {{ selectedNode.isRoot ? t('dictionary.add') : t('dictionary.addChild') }}
                </el-button>
                <el-button :icon="Edit" :disabled="selectedNode.isRoot" @click="onEditSelected">{{ t('common.edit') }}</el-button>
                <el-button plain type="danger" :icon="DeleteFilled" :disabled="selectedNode.isRoot" @click="onRemoveSelected">
                  {{ t('common.delete') }}
                </el-button>
              </div>
            </div>
            <p class="dm-detail-meta">
              <template v-if="!selectedNode.isRoot">
                <span v-if="selectedNode.remark">{{ selectedNode.remark }}</span>
                <span v-if="selectedNode.createBy">{{ t('dictionary.createBy') }}: {{ selectedNode.createBy }}</span>
                <span v-if="selectedNode.createTime">{{ selectedNode.createTime }}</span>
              </template>
              <template v-else>{{ t('dictionary.rootDescription') }}</template>
            </p>
          </header>

          <div class="dm-table-toolbar">
            <span class="dm-table-title">{{ t('dictionary.detailTitle') }}</span>
            <span v-if="multipleSelection.length" class="dm-selected-info">
              {{ t('dictionary.selectedCount', {count: multipleSelection.length}) }}
            </span>
            <el-button
              class="dm-batch-btn" plain type="danger" size="small" :icon="DeleteFilled"
              :disabled="!multipleSelection.length" @click="onDeleteBatch"
            >{{ t('dictionary.batchDelete') }}</el-button>
          </div>

          <div class="dm-table-wrap" v-loading="loading">
            <el-table
              v-if="childRows.length"
              :data="childRows"
              row-key="id"
              stripe
              height="100%"
              @selection-change="handleSelectionChange"
              @row-click="onRowClick"
            >
              <el-table-column type="selection" width="48"/>
              <el-table-column prop="dictionaryName" :label="t('dictionary.labelName')" sortable show-overflow-tooltip min-width="170">
                <template #default="{ row }">
                  <span class="dm-name-link" @click.stop="onSelectNode(row.id, true)">
                    <span class="dm-status-dot" :class="row.enableStatus === 1 ? 'is-on' : 'is-off'"></span>
                    <span class="dm-name-text">{{ row.dictionaryName }}</span>
                    <el-icon class="dm-drill-icon"><ArrowRight/></el-icon>
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="dictionaryValue" :label="t('dictionary.dictionaryValue')" show-overflow-tooltip min-width="130"/>
              <el-table-column prop="nameEn" :label="t('dictionary.nameEn')" show-overflow-tooltip min-width="130"/>
              <el-table-column prop="tagType" :label="t('dictionary.tagType')" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row.tagType" size="small" :type="row.tagType">{{ t('dictionary.tagTypes.' + row.tagType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remark" :label="t('dictionary.remark')" show-overflow-tooltip min-width="140"/>
              <el-table-column prop="enableStatus" :label="t('dictionary.isEnable')" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.enableStatus === 1 ? 'success' : 'info'">
                    {{ row.enableStatus === 1 ? t('common.yes') : t('common.no') }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="sort" :label="t('dictionary.sort')" sortable width="90"/>
              <el-table-column prop="createBy" :label="t('dictionary.createBy')" show-overflow-tooltip min-width="130"/>
              <el-table-column prop="createTime" :label="t('dictionary.createTime')" width="170" show-overflow-tooltip/>
              <el-table-column fixed="right" width="170" :label="t('dictionary.operation')" class-name="dm-op-col">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" :icon="Edit" @click.stop="onEdit(row)">{{ t('common.edit') }}</el-button>
                  <el-button size="small" text type="danger" :icon="DeleteFilled" @click.stop="onRemove(row)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else :description="hasFilter ? t('dictionary.noMatch') : t('dictionary.emptyChildren')" :image-size="110">
              <el-button v-if="hasFilter" :icon="RefreshLeft" @click="resetFilter">{{ t('dictionary.clearFilter') }}</el-button>
              <el-button v-else type="primary" :icon="Plus" @click="onAdd">
                {{ selectedNode.isRoot ? t('dictionary.add') : t('dictionary.addChild') }}
              </el-button>
            </el-empty>
          </div>
        </div>
      </transition>
    </section>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogFormVisible"
      :title="editForm.id ? t('dictionary.editTitle') : t('dictionary.addTitle')"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="sttFormRef" label-width="100px" :model="editForm" :rules="rules">
        <el-form-item prop="dictionaryName" :label="t('dictionary.name')">
          <el-input v-model="editForm.dictionaryName" :placeholder="t('dictionary.name')"/>
        </el-form-item>
        <el-form-item prop="dictionaryValue" :label="t('dictionary.dictionaryValue')">
          <el-input v-model="editForm.dictionaryValue" :placeholder="t('dictionary.valuePlaceholder')"/>
        </el-form-item>
        <el-form-item prop="nameEn" :label="t('dictionary.nameEn')">
          <el-input v-model="editForm.nameEn" :placeholder="t('dictionary.nameEnPlaceholder')"/>
        </el-form-item>
        <el-form-item prop="tagType" :label="t('dictionary.tagType')">
          <el-select v-model="editForm.tagType" clearable :placeholder="t('dictionary.tagTypePlaceholder')" style="width: 100%">
            <el-option v-for="c in tagTypeOptions" :key="c" :label="t('dictionary.tagTypes.' + c)" :value="c"/>
          </el-select>
        </el-form-item>
        <el-form-item prop="remark" :label="t('dictionary.remark')">
          <el-input v-model="editForm.remark" :placeholder="t('dictionary.remark')"/>
        </el-form-item>
        <el-form-item prop="enableStatus" :label="t('dictionary.isEnable')">
          <el-radio-group v-model="editForm.enableStatus">
            <el-radio :value="1">{{ t('common.yes') }}</el-radio>
            <el-radio :value="0">{{ t('common.no') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="sort" :label="t('dictionary.sortLabel')">
          <el-input-number v-model="editForm.sort" :min="0" :max="9999" controls-position="right" style="width: 100%"/>
        </el-form-item>
        <el-form-item :label="t('dictionary.parent')">
          <el-cascader
            v-model="editForm.parentId" clearable
            :placeholder="t('common.selectNode')"
            :props="defaultProps" :options="cascaderOptions" :show-all-levels="false"
            style="width: 100%"
            @change="handleChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogFormVisible = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="doUpdate(sttFormRef)">{{ t('common.confirm') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, reactive, ref} from "vue";
import {listByTree, add, deleteBatch, update} from "@/api/system/sys-dictionary-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {ElForm, ElMessageBox, ElTree} from "element-plus";
import {
  ArrowDownBold, ArrowRight, ArrowRightBold, Collection, DeleteFilled, Edit,
  FolderOpened, Plus, Refresh, RefreshLeft, Search
} from "@element-plus/icons-vue";
import {useI18n} from "vue-i18n";
import {useDictionaryStore} from "@/stores/dictionary";

const {t} = useI18n();
const dictionaryStore = useDictionaryStore();

type FormInstance = InstanceType<typeof ElForm>
type DictNode = DictionaryModel & { isRoot?: boolean }

const ROOT_ID = '0';

const treeProps = {label: 'dictionaryName', children: 'children'};
// 级联选择框配置
const defaultProps = {
  value: 'id',
  label: 'dictionaryName',
  children: 'children',
  checkStrictly: true
}

const sttFormRef = ref<FormInstance>();
const treeRef = ref<InstanceType<typeof ElTree>>();
const records = ref<DictNode[]>([]);
const loading = ref<boolean>(false);
const isFirstLoad = ref<boolean>(true);
// 筛选条件（客户端过滤，保留祖先链路）
const keyword = ref<string>('');
const statusFilter = ref<number | ''>('');
// 选中节点与展开状态
const selectedId = ref<string>(ROOT_ID);
const expandedKeys = ref<string[]>([ROOT_ID]);
const multipleSelection = ref<DictNode[]>([]);
// 表单参数
const editForm = ref<DictionaryModel>({});
const dialogFormVisible = ref<boolean>(false);
// 表单校验规则
const rules = reactive({
  dictionaryName: [{required: true, min: 2, max: 30, message: t('common.lengthLimit', {min: 2, max: 30}), trigger: 'blur'}],
  enableStatus: [{required: true, message: t('common.selectEnableStatus'), trigger: 'blur'}],
  remark: [{min: 2, max: 255, message: t('common.lengthLimit', {min: 2, max: 255}), trigger: 'blur'}]
});
const enableStatusList = ref<any>([
  {label: 'common.enable', value: 1},
  {label: 'common.disable', value: 0}
]);
const tagTypeOptions = ['primary', 'success', 'info', 'warning', 'danger'];

// ---------- 计算属性 ----------

const activeStatus = computed(() =>
    statusFilter.value === '' || statusFilter.value == null ? undefined : statusFilter.value);

const hasFilter = computed(() => keyword.value.trim() !== '' || activeStatus.value !== undefined);

const matches = (node: DictNode): boolean => {
  const kw = keyword.value.trim().toLowerCase();
  const kwOk = !kw || (node.dictionaryName || '').toLowerCase().includes(kw);
  const stOk = activeStatus.value === undefined || node.enableStatus === activeStatus.value;
  return kwOk && stOk;
};

// 保留匹配节点及其祖先链路
const pruneTree = (nodes: DictNode[]): DictNode[] => {
  if (!hasFilter.value) return nodes;
  const result: DictNode[] = [];
  for (const node of nodes) {
    const children = pruneTree(node.children || []);
    if (matches(node) || children.length) {
      result.push({...node, children});
    }
  }
  return result;
};

const filteredRecords = computed(() => pruneTree(records.value));

const rootNode = computed<DictNode>(() => ({
  id: ROOT_ID,
  dictionaryName: t('dictionary.rootNode'),
  isRoot: true,
  children: filteredRecords.value
}));

const treeData = computed(() => [rootNode.value]);

const hasData = computed(() => records.value.length > 0);

const flatten = (nodes: DictNode[]): DictNode[] =>
    nodes.flatMap(n => [n, ...flatten(n.children || [])]);

const allNodes = computed(() => flatten(records.value));
const totalCount = computed(() => allNodes.value.length);
const enabledCount = computed(() => allNodes.value.filter(n => n.enableStatus === 1).length);
const disabledCount = computed(() => totalCount.value - enabledCount.value);

const findNode = (nodes: DictNode[], id: string): DictNode | undefined => {
  for (const n of nodes) {
    if (n.id === id) return n;
    const found = findNode(n.children || [], id);
    if (found) return found;
  }
  return undefined;
};

const findPath = (nodes: DictNode[], id: string, trail: DictNode[] = []): DictNode[] | null => {
  for (const n of nodes) {
    const next = [...trail, n];
    if (n.id === id) return next;
    const found = findPath(n.children || [], id, next);
    if (found) return found;
  }
  return null;
};

const selectedNode = computed<DictNode>(() => {
  if (selectedId.value === ROOT_ID) return rootNode.value;
  return findNode(records.value, selectedId.value) || rootNode.value;
});

const breadcrumb = computed<DictNode[]>(() => {
  if (selectedNode.value.isRoot) return [rootNode.value];
  const path = findPath(records.value, selectedNode.value.id!) || [];
  return [rootNode.value, ...path];
});

// 右侧表格与左侧树保持一致的筛选结果；剥离 children 避免 el-table 渲染为树表
const childRows = computed<DictNode[]>(() => {
  const children = selectedNode.value.isRoot
      ? filteredRecords.value
      : (findNode(filteredRecords.value, selectedNode.value.id!)?.children || []);
  return children.map(({children: _ignored, ...rest}) => rest);
});

const collectExpandable = (nodes: DictNode[]): string[] =>
    nodes.flatMap(n => n.children?.length ? [n.id!, ...collectExpandable(n.children)] : []);

const allExpandableKeys = computed(() => [ROOT_ID, ...collectExpandable(records.value)]);

const isExpandAll = computed(() =>
    allExpandableKeys.value.every(k => expandedKeys.value.includes(k)));

// 编辑时排除自身子树，避免把节点挂到自己的后代下
const cascaderOptions = computed<DictNode[]>(() => {
  if (!editForm.value.id) return records.value;
  const exclude = (nodes: DictNode[]): DictNode[] => nodes
      .filter(n => n.id !== editForm.value.id)
      .map(n => ({...n, children: exclude(n.children || [])}));
  return exclude(records.value);
});

// ---------- 数据加载 ----------

const loadTableData = (): void => {
  loading.value = true;
  reqCommonFeedback(listByTree({}), (data: DictNode[]) => {
    records.value = data || [];
    loading.value = false;
    afterLoad();
  });
};

const afterLoad = () => {
  if (isFirstLoad.value) {
    expandedKeys.value = [...allExpandableKeys.value];
    isFirstLoad.value = false;
  } else {
    const valid = new Set(allExpandableKeys.value);
    expandedKeys.value = expandedKeys.value.filter(k => valid.has(k));
  }
  // 选中节点被删除时回退到根
  if (selectedId.value !== ROOT_ID && !findNode(records.value, selectedId.value)) {
    selectedId.value = ROOT_ID;
  }
  expandAncestors(selectedId.value);
  multipleSelection.value = [];
  nextTick(() => treeRef.value?.setCurrentKey(selectedId.value));
};

const expandAncestors = (id: string) => {
  const path = findPath(records.value, id) || [];
  const keys = new Set(expandedKeys.value);
  keys.add(ROOT_ID);
  path.slice(0, -1).forEach(n => keys.add(n.id!));
  expandedKeys.value = [...keys];
};

// ---------- 树交互 ----------

const onNodeClick = (data: DictNode): void => {
  onSelectNode(data.id!);
};

const onSelectNode = (id: string, expandSelf: boolean = false): void => {
  selectedId.value = id;
  multipleSelection.value = [];
  expandAncestors(id);
  if (expandSelf && !expandedKeys.value.includes(id)) {
    expandedKeys.value = [...expandedKeys.value, id];
  }
  nextTick(() => treeRef.value?.setCurrentKey(id));
};

const onNodeExpand = (data: DictNode): void => {
  if (!expandedKeys.value.includes(data.id!)) {
    expandedKeys.value = [...expandedKeys.value, data.id!];
  }
};

const onNodeCollapse = (data: DictNode): void => {
  expandedKeys.value = expandedKeys.value.filter(k => k !== data.id);
};

const onToggleExpand = (): void => {
  const expand = !isExpandAll.value;
  expandedKeys.value = expand ? [...allExpandableKeys.value] : [ROOT_ID];
  const store = (treeRef.value as any)?.store;
  if (!store) return;
  Object.values(store.nodesMap).forEach((node: any) => {
    if (node.data?.isRoot) {
      node.expand();
    } else if (expand) {
      node.expand();
    } else {
      node.collapse();
    }
  });
};

const resetFilter = (): void => {
  keyword.value = '';
  statusFilter.value = '';
};

// ---------- 表格交互 ----------

const handleSelectionChange = (arr: DictNode[]): void => {
  multipleSelection.value = arr;
};

const onRowClick = (row: DictNode, column: any): void => {
  if (!column || column.type === 'selection' || String(column.className || '').includes('dm-op-col')) return;
  onSelectNode(row.id!, true);
};

// ---------- 增删改 ----------

const onAdd = (): void => {
  editForm.value = {
    parentId: selectedId.value === ROOT_ID ? undefined : selectedId.value,
    enableStatus: 1,
    sort: 0
  };
  dialogFormVisible.value = true;
};

const onEdit = (row: DictNode): void => {
  editForm.value = {
    id: row.id,
    parentId: row.parentId === ROOT_ID ? undefined : row.parentId,
    dictionaryName: row.dictionaryName,
    dictionaryValue: row.dictionaryValue,
    nameEn: row.nameEn,
    tagType: row.tagType,
    remark: row.remark,
    enableStatus: row.enableStatus,
    sort: row.sort
  };
  dialogFormVisible.value = true;
};

const onEditSelected = (): void => {
  onEdit(selectedNode.value);
};

const onRemoveSelected = (): void => {
  onRemove(selectedNode.value);
};

const onRemove = (row: DictNode): void => {
  const full = findNode(records.value, row.id!) || row;
  const message = full.children?.length
      ? t('dictionary.confirmDeleteWithChildren')
      : t('dictionary.confirmDelete');
  ElMessageBox.confirm(message, t('common.tip'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning',
  }).then(() => {
    reqSuccessFeedback(deleteBatch([row.id]), t('common.deleteSuccess'), () => {
      // 当前选中节点处于被删除子树内时，回退到其父级
      const path = findPath(records.value, selectedId.value) || [];
      if (path.some(n => n.id === row.id)) {
        selectedId.value = row.parentId || ROOT_ID;
      }
      dictionaryStore.loadCache(true);
      loadTableData();
    });
  }).catch(() => {
  });
};

const onDeleteBatch = (): void => {
  const ids = multipleSelection.value.map(item => item.id!);
  if (!ids.length) return;
  ElMessageBox.confirm(t('dictionary.confirmDeleteSelected'), t('common.tip'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning',
  }).then(() => {
    reqSuccessFeedback(deleteBatch(ids), t('common.deleteSuccess'), () => {
      const deleted = new Set(ids);
      const path = findPath(records.value, selectedId.value) || [];
      if (path.some(n => deleted.has(n.id!))) {
        selectedId.value = ROOT_ID;
      }
      dictionaryStore.loadCache(true);
      loadTableData();
    });
  }).catch(() => {
  });
};

const doUpdate = (formEl: FormInstance | undefined): void => {
  if (!formEl) return;
  formEl.validate((valid) => {
    if (!valid) return;
    const payload = {...editForm.value, parentId: editForm.value.parentId || ROOT_ID};
    if (!payload.id) {
      reqSuccessFeedback(add(payload), t('common.addSuccess'), () => {
        dialogFormVisible.value = false;
        // 新增后定位到父节点，便于查看新子项
        selectedId.value = payload.parentId!;
        dictionaryStore.loadCache(true);
        loadTableData();
      });
    } else {
      reqSuccessFeedback(update(payload), t('common.updateSuccess'), () => {
        dialogFormVisible.value = false;
        dictionaryStore.loadCache(true);
        loadTableData();
      });
    }
  });
};

const handleChange = (data: any): void => {
  editForm.value.parentId = data?.length ? data[data.length - 1] : undefined;
};

onMounted(() => {
  dictionaryStore.loadCache();
  loadTableData();
});
</script>

<style scoped>
.dm-page {
  display: flex;
  gap: 14px;
  height: 100%;
  min-height: 480px;
}

.dm-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  box-shadow: var(--el-box-shadow-lighter);
  overflow: hidden;
  animation: dm-rise 0.35s ease both;
}

.dm-tree-panel {
  width: 300px;
  flex-shrink: 0;
  animation-delay: 0.02s;
}

.dm-detail-panel {
  flex: 1;
  min-width: 0;
  animation-delay: 0.08s;
}

@keyframes dm-rise {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

/* ---------- 左面板 ---------- */

.dm-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: linear-gradient(135deg, var(--el-color-primary-light-9), transparent 65%);
}

.dm-panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.dm-title-icon {
  font-size: 17px;
  color: var(--el-color-primary);
}

.dm-count-badge {
  min-width: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  font-size: 11px;
  font-weight: 600;
  line-height: 18px;
  text-align: center;
}

.dm-panel-tools {
  display: flex;
  gap: 2px;
}

.dm-filter {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px 10px;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.dm-filter-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dm-status-select {
  flex: 1;
}

.dm-tree-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 8px 6px;
}

.dm-tree-scroll::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.dm-tree-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.dm-tree-scroll::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 3px;
}

.dm-tree-scroll::-webkit-scrollbar-thumb:hover {
  background: var(--el-text-color-disabled);
}

.dm-tree :deep(.el-tree-node__content) {
  height: 34px;
  border-radius: 6px;
  transition: background-color 0.18s ease;
}

.dm-tree :deep(.el-tree-node__content:hover) {
  background: var(--el-fill-color-light);
}

.dm-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: var(--el-color-primary-light-9);
  box-shadow: inset 3px 0 0 var(--el-color-primary);
}

.dm-tree :deep(.el-tree-node.is-current > .el-tree-node__content .dm-node-label) {
  color: var(--el-color-primary);
  font-weight: 600;
}

.dm-tree-node {
  display: flex;
  align-items: center;
  gap: 7px;
  flex: 1;
  min-width: 0;
  padding-right: 6px;
  font-size: 13px;
}

.dm-node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--el-text-color-regular);
}

.dm-tree-node.is-disabled .dm-node-label {
  color: var(--el-text-color-secondary);
}

.dm-node-root-icon {
  font-size: 15px;
  color: var(--el-color-primary);
}

.dm-node-count {
  flex-shrink: 0;
  min-width: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--el-fill-color);
  color: var(--el-text-color-secondary);
  font-size: 11px;
  line-height: 16px;
  text-align: center;
}

.dm-status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dm-status-dot.is-on {
  background: #67c23a;
  box-shadow: 0 0 6px rgba(103, 194, 58, 0.55);
}

.dm-status-dot.is-off {
  background: var(--el-text-color-disabled);
}

html.dark .dm-status-dot.is-on {
  background: #95d475;
  box-shadow: 0 0 6px rgba(149, 212, 117, 0.45);
}

.dm-no-match {
  padding: 18px 10px;
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.dm-tree-footer {
  display: flex;
  gap: 14px;
  padding: 8px 14px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-lighter);
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.dm-stat {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

/* ---------- 右面板 ---------- */

.dm-detail-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.dm-detail-header {
  padding: 12px 18px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: linear-gradient(160deg, var(--el-color-primary-light-9), transparent 55%);
}

.dm-breadcrumb {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.dm-breadcrumb-item {
  padding: 1px 5px;
  border-radius: 4px;
  cursor: pointer;
  transition: color 0.15s ease, background-color 0.15s ease;
}

.dm-breadcrumb-item:hover {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.dm-breadcrumb-item.is-active {
  color: var(--el-text-color-primary);
  font-weight: 600;
  cursor: default;
}

.dm-breadcrumb-item.is-active:hover {
  background: transparent;
}

.dm-breadcrumb-sep {
  color: var(--el-text-color-disabled);
}

.dm-detail-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.dm-detail-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.dm-detail-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.3px;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dm-child-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.dm-detail-actions {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
}

.dm-detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.dm-table-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 18px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.dm-table-title {
  position: relative;
  padding-left: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.dm-table-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background: var(--el-color-primary);
}

.dm-selected-info {
  font-size: 12px;
  color: var(--el-color-primary);
}

.dm-batch-btn {
  margin-left: auto;
}

.dm-table-wrap {
  position: relative;
  flex: 1;
  min-height: 0;
  padding: 0 6px 6px;
}

.dm-table-wrap :deep(.el-table__row) {
  cursor: pointer;
}

.dm-table-wrap :deep(.el-empty) {
  height: 100%;
  justify-content: center;
}

.dm-name-link {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  max-width: 100%;
  color: var(--el-color-primary);
  font-weight: 500;
  cursor: pointer;
}

.dm-name-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dm-name-link:hover .dm-name-text {
  text-decoration: underline;
}

.dm-drill-icon {
  font-size: 12px;
  opacity: 0;
  transform: translateX(-3px);
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.dm-name-link:hover .dm-drill-icon {
  opacity: 1;
  transform: none;
}

/* ---------- 过渡 ---------- */

.dm-swap-enter-active,
.dm-swap-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.dm-swap-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.dm-swap-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* ---------- 响应式 ---------- */

@media (max-width: 960px) {
  .dm-page {
    flex-direction: column;
  }

  .dm-tree-panel {
    width: 100%;
    max-height: 42%;
  }

  .dm-detail-panel {
    flex: 1;
  }
}
</style>
