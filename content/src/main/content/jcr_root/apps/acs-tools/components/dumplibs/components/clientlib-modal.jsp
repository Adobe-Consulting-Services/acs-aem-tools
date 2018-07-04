<div is="clientlib-modal" inline-template>
<div>
<div class="coral-Modal" v-bind:class="{active: open}">
    <div class="coral-Modal-header">
        <i class="coral-Modal-typeIcon coral-Icon coral-Icon--sizeS"></i>
        <h2 class="coral-Modal-title coral-Heading coral-Heading--2">{{header ? header : 'Clientlib/s detail'}}</h2>
        <button type="button" class="coral-MinimalButton coral-Modal-closeButton" title="Close" @click="open=false">
            <i class="coral-Icon coral-Icon--sizeXS coral-Icon--close coral-MinimalButton-icon "></i>
        </button>
    </div>
    <div class="coral-Modal-body">
        <div class="coral-Wait coral-Wait--large wait-centered" v-bind:style="{ display: loading ? 'block' : 'none'}"></div>
        <div class="coral-TabPanel" data-init='tabs' >
            <nav class="coral-TabPanel-navigation">
                <a class="coral-TabPanel-tab is-active" data-toggle="tab">JS Libs</a>
                <a class="coral-TabPanel-tab" data-toggle="tab">CSS Libs</a>
            </nav>
            <div class="coral-TabPanel-content">
                <section class="coral-TabPanel-pane is-active">
                    <div  v-if="clientlibs.js && clientlibs.js.length" v-for="(lib, index) in clientlibs.js" class="clientlib">
                        <list :value="lib" :header="'Clientlib '+(index+1)+' of '+clientlibs.js.length"></list>
                    </div>
                    <div v-if="!clientlibs.js || clientlibs.js.length === 0" class="coral-Well">
                        No JS clientlib found
                    </div>
                </section>
                <section class="coral-TabPanel-pane">
                    <div v-if="clientlibs.css && clientlibs.css.length" v-for="(lib, index) in clientlibs.css" class="clientlib">
                        <list :value="lib" :header="'Clientlib '+(index+1)+' of '+clientlibs.css.length"></list>
                    </div>
                    <div v-if="!clientlibs.css || clientlibs.css.length === 0" class="coral-Well">
                        No CSS clientlib found
                    </div>
                </section>
            </div>
        </div>
    </div>
</div>
<div class="coral-Modal-backdrop" v-bind:style="{display: open ? 'block' : 'none'}" aria-hidden="true"></div>
</div>
</div>