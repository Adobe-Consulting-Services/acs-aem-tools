<div is="clientlib-table" inline-template>
    <div>
        <form class="coral-Form coral-Form--vertical">
            <section class="coral-Form-fieldset">
                <h3 class="coral-Form-fieldset-legend">Filter Options</h3>
                <text-field label="Filter by path" :value.sync="filters.path" quicktip="List will filter as you type"></text-field>
                <text-field label="Filter by category" :value.sync="filters.categories" quicktip="List will filter as you type"></text-field>
                <text-field label="Filter by channels" :value.sync="filters.channels" quicktip="List will filter as you type"></text-field>
                <radio-field label="Filter by Type" :value.sync="filters.types" :options="['JS', 'CSS', '']" empty-option="Both" quicktip="List will filter as you type"></text-field>

            </section>
        </form>
        <div class="coral-Wait coral-Wait--medium wait-centered" v-bind:style="{ display: loading ? 'block' : 'none'}"></div>
        <table class="coral-Table coral-Table--bordered" v-if="filteredClientlibs.length > 0">
            <thead class="coral-Table-head">
                <tr class="coral-Table-row">
                    <th class="coral-Table-headerCell col-7">Path</th>
                    <th class="coral-Table-headerCell col-1">Types</th>
                    <th class="coral-Table-headerCell col-2">Categories</th>
                    <th class="coral-Table-headerCell col-2">Channels</th>
                </tr>
            </thead>
            <tbody class="coral-Table-body">
                <tr class="coral-Table-row" v-for="clientlib in filteredClientlibs">
                    <td class="coral-Table-cell col-6"><a class="coral-Link" href="javascript:void(0)" @click="openModalByPath(clientlib, clientlib.path, {'path': clientlib.path})">{{clientlib.path}}</a></td>
                    <td class="coral-Table-cell col-1">{{clientlib.types.join(', ')}}</td>
                    <td class="coral-Table-cell col-2">
                        <span v-for="(catig, index) in clientlib.categories">
                            <a class="coral-Link" href="javascript:void(0)"  @click="openModalByCategory(clientlib, catig, {'categories': catig})">
                                {{catig}}
                            </a>
                            <span v-if="index !== clientlib.categories.length - 1">,&nbsp;&nbsp;</span>
                        </span>
                    </td>
                    <td class="coral-Table-cell col-2">{{clientlib.channels.join(', ')}}</td>
                </tr>
            </tbody>
        </table>
        <div class="coral-Well" v-if="!loading && filteredClientlibs.length === 0 && !error">
            There are no clientlibs that match your search criteria
        </div>
        <div class="coral-Well" v-if="error">
            An error occured, check console for more detail.
        </div>
    </div>
</div>