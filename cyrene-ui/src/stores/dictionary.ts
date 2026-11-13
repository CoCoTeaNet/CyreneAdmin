import {defineStore} from "pinia";
import {cache} from "@/api/system/sys-dictionary-api";
import {ApiResultEnum} from "@/api/ApiResultEnum";
import i18n, {LOCALE_EN} from "@/i18n";

const CACHE_KEY = "dictionaryCache";

/**
 * 读取本地缓存的字典，刷新后可立即展示
 */
const readLocalCache = (): Record<string, DictionaryModel[]> => {
    try {
        const raw = localStorage.getItem(CACHE_KEY);
        return raw ? JSON.parse(raw) : {};
    } catch {
        return {};
    }
}

/**
 * 按当前语言取显示标签：英文优先 nameEn，中文优先 dictionaryName
 */
const pickLabel = (item: DictionaryModel): string => {
    const en = i18n.global.locale.value === LOCALE_EN;
    return (en ? (item.nameEn || item.dictionaryName) : (item.dictionaryName || item.nameEn)) || '';
}

/**
 * 字典码值多为数字字符串，转成数字以兼容数值型表单模型
 */
const normalizeValue = (value?: string): string | number => {
    if (value === undefined || value === '') {
        return '';
    }
    return !isNaN(Number(value)) ? Number(value) : value;
}

export const useDictionaryStore = defineStore('dictionary', {
    state: () => ({
        map: readLocalCache() as Record<string, DictionaryModel[]>,
        loaded: false
    }),

    actions: {
        /**
         * 加载启用字典缓存（已加载则直接返回，force 强制刷新）
         */
        async loadCache(force = false) {
            if (this.loaded && !force) {
                return;
            }
            const res: any = await cache();
            if (res.code === ApiResultEnum.SUCCESS) {
                this.map = res.data || {};
                this.loaded = true;
                localStorage.setItem(CACHE_KEY, JSON.stringify(this.map));
            }
        },

        /**
         * 失效缓存（字典维护保存后调用，下次展示重新拉取）
         */
        invalidate() {
            this.map = {};
            this.loaded = false;
            localStorage.removeItem(CACHE_KEY);
        },

        /**
         * 取某分类下的子项
         */
        getItems(key: string): DictionaryModel[] {
            return this.map[key] || [];
        },

        /**
         * 取显示标签（按当前语言），未命中返回原始值
         */
        getLabel(key: string, value: any): string {
            const item = this.getItems(key).find(i => String(i.dictionaryValue) === String(value));
            if (!item) {
                return value === undefined || value === null ? '' : String(value);
            }
            return pickLabel(item);
        },

        /**
         * 取标签颜色（el-tag type）
         */
        getTagType(key: string, value: any): string {
            const item = this.getItems(key).find(i => String(i.dictionaryValue) === String(value));
            return item?.tagType || '';
        },

        /**
         * 取下拉/单选选项（label 按当前语言，value 兼容数值模型）
         */
        getOptions(key: string): { label: string; value: string | number }[] {
            return this.getItems(key).map(i => ({
                label: pickLabel(i),
                value: normalizeValue(i.dictionaryValue)
            }));
        }
    }
});