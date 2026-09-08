import i18n from '@/i18n'

const commonTypes = {
    getIsCn: getIsCn,
    getIsList: getIsList
}

export function getIsCn(s: number) {
    switch (s) {
        case 0:
            return i18n.global.t('common.no');
        case 1:
            return i18n.global.t('common.yes');
    }
    return s;
}
export function getIsList() {
    return [
        // label 存 i18n key，渲染时由组件统一翻译
        {label: 'common.no', value: 0},
        {label: 'common.yes', value: 1},
    ]
}

export default commonTypes;