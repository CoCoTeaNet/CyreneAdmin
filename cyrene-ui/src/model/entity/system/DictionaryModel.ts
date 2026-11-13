/**
 * 字典模型
 */
interface DictionaryModel {
    id?: string,
    parentId?: string,
    dictionaryName?: string,
    dictionaryValue?: string,
    nameEn?: string,
    tagType?: string,
    remark?: string,
    enableStatus?: number,
    sort?: number,
    children?: DictionaryModel[]
}