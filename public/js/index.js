$(function() {
  addDefaultAttributes();
  formatUnitSwitchButtons();
  $(".show_btc").click();
  setAllLinks();

  // show to Top button for large scroll sites 
  if ( ($(window).height() + 100) < $(document).height() ) {
    $('#top-link-block').removeClass('hidden').affix({
        // how far to scroll down before link "slides" into view
        offset: {top:100}
    });
  }
});

function formatUnitSwitchButtons()
{
  formatButton("show_btc", "BTC", 8);
  formatButton("show_sat", "sat", 0);
  formatButton("show_bit", "bit", 2);
  formatButton("show_mil", "mBTC", 5);
}

function setAllLinks()
{
  setLinks("bge_address", "address");
  setLinks("bge_block", "block");
  setLinks("bge_tx_hash", "transaction");
  setLinks("bge_wallet", "wallet");   
}

function setLinks(className, url)  
{
  $("."+className).each(function(index, element){setLink(element, url);});
}

function setLink(element, url)
{
  var a = document.createElement("a");
  a.href = "/"+url+"/"+element.getAttribute("search");
  a.innerHTML = element.innerHTML;
  element.innerHTML = "";
  element.appendChild(a);
}

function setSelected(className)
{
  $(".show_unit").removeClass("active")
  $("."+className).addClass("active")
}

function formatButton(className, label, positions)
{
  $("."+className).html(label).removeClass("hidden").click(function(){formatAllValues(positions); setSelected(className); return false;})
}

function formatAllValues(d)
{
  $(".bge_value").each(function(i, v){formatBitcoinValues(i,v,d)})
}  

function addDefaultAttributes()
{
  $(".bge_block, .bge_tx_hash, .bge_wallet, .bge_address").each(function(i,e){
    if(e.getAttribute("search")==null)
      e.setAttribute("search", e.innerHTML);
  });
  
  $(".bge_value").each(function(i,e){
    if(e.getAttribute("satoshis")==null)
      e.setAttribute("satoshis", e.innerHTML);
  });
}

function formatBitcoinValues(index, element, divisor)
{
  var e = (element.firstChild.tagName=="A" ? element.firstChild : element);
  e.innerHTML = parseFloat(element.getAttribute("satoshis")/Math.pow(10, divisor)).toFixed(divisor);
}
