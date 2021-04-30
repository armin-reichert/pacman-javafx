<#-- template to create the name of the runner script -->
<#if osName?upper_case?contains("WIN")>
run.cmd
<#else>
run.sh
</#if>