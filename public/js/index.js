$(function() {
  onLoaded();
});

function onLoaded()
{
  addDefaultAttributes(".bge_block,.bge_address,.bge_wallet, .bge_tx", "search");
  addDefaultAttributes(".bge_value", "satoshis");
  formatButton("show_btc", "BTC", 8);
  formatButton("show_sat", "sat", 0);
  formatButton("show_bit", "bit", 2);
  formatButton("show_mil", "mBTC", 5);
  setLinks("bge_address", "address");
  setLinks("bge_block", "block");
  setLinks("bge_tx", "transaction");
  setLinks("bge_wallet", "wallet");
  formatAllNumbers();
  formatAllDates();
  formatAllTimes();
  $(".show_btc").click();
  if ( ($(window).height() + 100) < $(document).height() ) {
    $('#top-link-block').removeClass('hidden').affix({
        offset: {top:100}
    });
  }
}

function dateConverter(UNIX_timestamp){
  var a = new Date(UNIX_timestamp*1000);
  var year = a.getFullYear();
  var month = a.getMonth()+1;
  if (month < 10)
    month = "0" + month
  var date = a.getDate();
  if (date < 10)
    date = "0" + date
  var hour = a.getHours();
  if (hour < 10)
    hour = "0" + hour
  var min = a.getMinutes();
  if (min < 10)
    min = "0" + min


  var time = date + '/' + month + '/' + year + " " + hour + ':' + min;
  return time;
}

function timeConverter(seconds){
  var hours = Math.floor(seconds / 3600);
  var minutes = Math.floor((seconds % 3600) / 60);
  var secondsT = seconds % 60;
  
  return (hours > 0  ? hours + "h " : "") + 
         (minutes > 0 ? minutes  + "m " : "")  + 
         (secondsT + "s")
  ;
}

function formatDate(element) {
  
  element.innerHTML = dateConverter(parseInt(element.innerHTML));
}

function formatTime(element) {
  element.innerHTML = timeConverter(parseInt(element.innerHTML));
} 

function numberWithCommas(x) {
    var parts = x.toString().split(".");
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    return parts.join(".");
}

function setLinks(className, url)  
{
  $("."+className).each(function(index, element){setLink(element, url);});
}

function setLink(element, url)
{
  
  var html = element.innerHTML;
  var arrSearch = html.split(",");
  var size = arrSearch.length;
  var isMulti = size>1;
  var width = Math.floor(isMulti?(80/size):100);
  arrSearch.forEach(function(e,i){
    arrSearch[i] = (size > 1 ? "<span class='elliptic' style='display:inline-block;max-width:"+width+"%;'>&nbsp;":"<span>") + e + "</span>";

  });
  html = (size > 1 ? "<span class='elliptic' style='display:inline-block;max-width:10%;'>1 of&nbsp;</span>":"")  + arrSearch.join("");
  var search = element.getAttribute("search");  
  var a = document.createElement("a");
  a .style.width="100%";
  a.href = "/"+url+"/"+search;
  a.innerHTML = html;
  //a.style.display="inline-block";
  element.innerHTML = "";
  element.appendChild(a);
  
}

function setSelected(className)
{
  $(".show_unit").removeClass("active");
  $("."+className).addClass("active");
}

function formatButton(className, label, positions)
{
  $("."+className).html(label).removeClass("hidden").click(function(){formatAllValues(positions); setSelected(className); return false;});
}

function formatAllValues(d)
{
  $(".bge_value").each(function(i, v){formatBitcoinValues(i,v,d);});
}

function formatAllNumbers()
{
  $(".bge_number,.bge_block").each(function(i, v){formatNumber(v);});
}

function formatAllDates()
{
  $(".bge_date").each(function(i,v){formatDate(v);});
}

function formatAllTimes()
{
  $(".bge_time").each(function(i,v){formatTime(v);});
}

function addDefaultAttributes(selector, attribute)
{
  $(selector).each(function(i,e){ if(e.getAttribute(attribute)==null) e.setAttribute(attribute, e.innerHTML);});
}

function formatBitcoinValues(index, element, divisor)
{
  var e = (element.firstChild.tagName=="A" ? element.firstChild : element);
  e.innerHTML = numberWithCommas(parseFloat(element.getAttribute("satoshis")/Math.pow(10, divisor)).toFixed(divisor));
}

function formatNumber(element)
{ 
  var e = (element.firstChild.tagName=="A" ? element.firstChild : element);
  e.innerHTML = numberWithCommas(e.innerHTML);
}
