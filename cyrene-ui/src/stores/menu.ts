import { defineStore } from "pinia";
import { router } from "@/router";
// @ts-ignore
import md5 from 'js-md5';

export const useMenuStore = defineStore('menu', {
    state: () => ({
        isCollapseMenu: false,
        tabItems: [],
    }),

    actions: {
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
            this.tabItems.push({ name: '首页', id: md5(url), isActive: true, url: url });
            router.push({ path: url }).then(r => console.log(r));
        },
        /**
         * 设置是否折叠菜单
         */
        setCollapseMenu() {
            this.isCollapseMenu = !this.isCollapseMenu;
        },
        updateCollapseMenu(flag: boolean) {
            this.isCollapseMenu = flag;
        }
    }

});
