import { defineStore } from "pinia";
import { router } from "@/router";
// @ts-ignore
import md5 from 'js-md5';

// 折叠前先淡出菜单文字：EP 折叠会重建菜单 DOM，文字节点瞬间移除，需提前淡出
let fadeTimer: number | undefined;

export const useMenuStore = defineStore('menu', {
    state: () => ({
        isCollapseMenu: false,
        isLabelFading: false,
        tabItems: [],
        menus: []
    }),

    actions: {
        /**
         * 设置用户菜单
         *
         * @param menuList 菜单列表
         */
        setUserMenu(menuList: MenuModel[]) {
            this.menus = menuList;        
            localStorage.setItem("menus", JSON.stringify(menuList));
        },
        /** 
         * 移除标签页
         * 
         * @param id 标签页ID
         */
        removeTabItem(id: string) {
            let arr: TabItem[] = [];
            this.tabItems.forEach((item: TabItem) => {
                if (item.id != id) arr.push(item);
            });
            this.tabItems = arr;
            // 激活最后一项
            if (arr.length > 0) {
                let activeTab = arr[arr.length - 1];
                activeTab.isActive = true;
                router.push({ path: activeTab.url ? activeTab.url : '' }).then(r => console.log(r));
            } else {
                this.initTabItems();
            }
            localStorage.setItem("userTabs", JSON.stringify(this.tabItems));
        },
        addTabItem(tab: TabItem) {
            tab.id = md5(tab.url);
            if (this.tabItems.length === 0) {
                this.tabItems.push(tab);
            } else {
                let existFlag = false;
                this.tabItems.forEach((item: TabItem) => {
                    if (item.id == tab.id) {
                        existFlag = true;
                    }
                });
                if (!existFlag) {
                    this.tabItems.forEach((item: TabItem) => item.isActive = false);
                    this.tabItems.push(tab);
                } else {
                    this.tabItems.forEach((item: TabItem) => {
                        item.isActive = item.id === tab.id;
                    });
                }
            }
            localStorage.setItem("userTabs", JSON.stringify(this.tabItems));
        },
        initTabItems() {
            this.tabItems = [];
            this.goHome();
            localStorage.setItem("userTabs", JSON.stringify(this.tabItems));
        },
        loadTabItems() {
            let userTabs = localStorage.getItem("userTabs");
            this.tabItems = [];
            if (userTabs) {
                this.tabItems = JSON.parse(userTabs);
            } else {
                this.goHome();
            }
        },
        goHome() {
            let url: string = '/admin/home';
            // name 存 i18n key，渲染时由组件统一翻译
            this.tabItems.push({ name: 'route.home', id: md5(url), isActive: true, url: url });
            router.push({ path: url }).then(r => console.log(r));
        },
        /**
         * 设置是否折叠菜单
         */
        setCollapseMenu() {
            if (this.isLabelFading) {
                this.cancelLabelFade();
                return;
            }
            if (this.isCollapseMenu) {
                this.isCollapseMenu = false;
            } else {
                this.fadeThenCollapse();
            }
        },
        updateCollapseMenu(flag: boolean) {
            if (flag) {
                this.fadeThenCollapse();
            } else {
                this.cancelLabelFade();
                this.isCollapseMenu = false;
            }
        },
        fadeThenCollapse() {
            if (this.isCollapseMenu || this.isLabelFading) return;
            this.isLabelFading = true;
            fadeTimer = window.setTimeout(() => {
                fadeTimer = undefined;
                this.isLabelFading = false;
                this.isCollapseMenu = true;
            }, 160);
        },
        cancelLabelFade() {
            if (fadeTimer !== undefined) {
                window.clearTimeout(fadeTimer);
                fadeTimer = undefined;
            }
            this.isLabelFading = false;
        }
    }

});
