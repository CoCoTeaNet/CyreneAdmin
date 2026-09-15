import {createI18n} from 'vue-i18n'
import zhCN from './lang/zh-CN'
import enUS from './lang/en-US'

export const LOCALE_ZH = 'zh-CN'
export const LOCALE_EN = 'en-US'

/**
 * 读取本地存储的语言设置，默认中文
 */
const getSavedLocale = (): string => {
    const saved = localStorage.getItem('locale');
    if (saved === LOCALE_EN || saved === LOCALE_ZH) {
        return saved;
    }
    return LOCALE_ZH;
}

const i18n = createI18n({
    legacy: false,
    locale: getSavedLocale(),
    fallbackLocale: LOCALE_ZH,
    globalInjection: true,
    messages: {
        [LOCALE_ZH]: zhCN,
        [LOCALE_EN]: enUS,
    },
})

/**
 * 切换语言（会持久化到本地，刷新后保持）
 */
export function setLocale(locale: string) {
    if (locale !== LOCALE_ZH && locale !== LOCALE_EN) {
        locale = LOCALE_ZH;
    }
    i18n.global.locale.value = locale;
    localStorage.setItem('locale', locale);
}

/**
 * 翻译文案：
 * 若文本本身是 i18n key 则翻译，否则原样返回（兼容后端下发的已翻译文本）
 */
export function translate(text?: string): string {
    if (!text) {
        return '';
    }
    return i18n.global.te(text) ? i18n.global.t(text) : text;
}

export default i18n;