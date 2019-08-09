package example

import "fmt"

func main()  {
	var oneTypo = "It is <warning descr="ARTICLE_MISSING">friend</warning> of human"
	var oneSpellcheckTypo = "It is <warning descr="MORFOLOGIK_RULE_EN_US">frend</warning> of human"
	var fewTypos = "It <warning descr="IT_VBZ">are</warning> working for <warning descr="MUCH_COUNTABLE">much</warning> warnings"
	var ignoreTemplate = "It is %s friend"
	var notIgnoreOtherMistakes = "It is <warning descr="ARTICLE_MISSING">friend</warning>. <warning descr="And">But</warning> I have <warning descr="EN_A_VS_AN">a</warning> %s here"

	fmt.Sprintf("It is <warning descr="ARTICLE_MISSING">friend</warning> of human");
	fmt.Sprintf("It is <warning descr="MORFOLOGIK_RULE_EN_US">frend</warning> of human");
	fmt.Sprintf("It <warning descr="IT_VBZ">are</warning> working for <warning descr="MUCH_COUNTABLE">much</warning> warnings")
	fmt.Sprintf("It is %s friend", oneTypo)
	// TODO add format string support
	fmt.Sprintf("It is <warning descr="ARTICLE_MISSING">friend</warning>. <warning descr="And">But</warning> I have <warning descr="EN_A_VS_AN">a</warning> %s here", oneTypo)
}
