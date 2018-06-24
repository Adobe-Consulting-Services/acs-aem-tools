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

        <div v-for="(lib, index) in clientlibs" class="clientlib">
            <list :value="lib" :header="'Clientlib '+(index+1)+' of '+clientlibs.length"></list>
        </div>

    </div>
    <div class="coral-Modal-footer">
        <button type="button" class="coral-Button" data-dismiss="modal" @click="open=false">Close</button>
    </div>
</div>
<div class="coral-Modal-backdrop" v-bind:style="{display: open ? 'block' : 'none'}" aria-hidden="true"></div>
</div>
</div>