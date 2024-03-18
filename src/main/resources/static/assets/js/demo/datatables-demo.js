// Call the dataTables jQuery plugin
$(document).ready(function() {
  $('#dataTable').DataTable();
});

//拔掉搜尋且用第二欄位排序
  $('#dataTable_noSearch').DataTable({
  	"searching": false,
  	"order": [1, 'desc'],
  	"language": {
      "emptyTable": "無資料,請重新查詢"
    },
  	"stateSave": true
  });
  
  //拔掉搜尋且用第1欄位排序
  $('#dataTable_noSearch_first').DataTable({
  	"searching": false,
  	"order": [0, 'desc'],
  	"language": {
      "emptyTable": "無資料,請重新查詢"
    },
  	"stateSave": true
  });
  
  //拔掉搜尋且用第3欄位排序
  $('#dataTable_noSearch_3rd').DataTable({
  	"searching": false,
  	"order": [2, 'asc'],
  	"language": {
      "emptyTable": "無資料,請重新查詢"
    },
  	"stateSave": true
  });
  
  //拔掉搜尋且用第4欄位Desc排序
  $('#dataTable_noSearch_4rdDesc').DataTable({
  	"searching": false,
  	"order": [3, 'desc'],
  	"language": {
      "emptyTable": "無資料,請重新查詢"
    },
  	"stateSave": true
  });
