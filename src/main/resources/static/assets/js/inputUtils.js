/**
 * @author elliot
 */

/**
 * @description 輸入限制
 * class="type_w" 限制輸入純數字、英文大小寫
 * class="type_d" 限制輸入純數字(千分)
 * class="type_m" 限制輸入純數字(千分, 可負值)(只能最後輸入-值)
 * class="type_a" 限制輸入非中文
 * class="type_s" 限制輸入純數字字串
 */
 function setUpInputConstraint(className){
	$(document).on('keyup change monseup', className, function(e){
		let type;
		if($(this).attr('class').includes('type_w')){
			type = "w";
		}
		if($(this).attr('class').includes('type_d')){
			type = "d";
		}
		if($(this).attr('class').includes('type_a')){
			type = "a";
		}
		if($(this).attr('class').includes('type_s')){
			type = "s";
		}

		let value = e.target.value.trim().replaceAll(",", "");
		let cnReg = /[\u4E00-\u9FFF]/g;
		let numReg = /[0-9]/;
		let NegtiveNumReg = /[\-0-9]/;
		let numAlpReg = /[A-Za-z0-9_]/;
		let prune = true;

		while(prune){
			if(value==""){
				$(this).val(value);
				prune = false;
			}
			let firstword = value.substr(0,1);
			let lastword = value.substr(-1);
			// 純數字
			if(type=="d" || type=="s"){
				if(!numReg.exec(lastword)){
					value = value.substr(0, value.length-1);
				}
				else if(!NegtiveNumReg.exec(firstword)){
					value = value.substr(1, value.length);
				}
				else{
					prune = false;
				}
			}
			// 數字+字母
			else if(type=="w" && !numAlpReg.exec(lastword)){
				value = value.substr(0, value.length-1);
			}
			else if(type=="a"){
				value = value.replace(cnReg, '');
				$(this).val(value);
				prune = false;
			}
			else{
				prune = false;
			}
		}

		switch(type){
			case "d":
				if(!isNaN(value)){
					$(this).val(BigInt(value).toLocaleString());
				}
				else{
					$(this).val('');
				}
				break;
			case "w":
				$(this).val(value);
				break;
			case "a":
				$(this).val(value);
				break;
			case "s":
				$(this).val(value);
				break;
			default:
				// console.log('error');
				break;
		}
	})
}