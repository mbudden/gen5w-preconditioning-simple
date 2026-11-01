package com.paytondev.launchpad;

final class ScreenFlow {
    enum Page { PRECONDITIONING, ADVANCED, BROWSER }
    static final Page[] TABS = {Page.PRECONDITIONING, Page.ADVANCED};
    static Page back(Page page) {
        if (page == Page.BROWSER) return Page.ADVANCED;
        return page == Page.PRECONDITIONING ? null : Page.PRECONDITIONING;
    }
}
