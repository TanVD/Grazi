package example

/**
 * A group of *members*.
 *
 * This class has no useful logic; it's just a documentation example.
 *
 */
type ExampleClassWithNoTypos struct {
	name string
}

// Creates an empty group.
// The name of the group.
func NewExampleClassWithNoTypos(name string) *ExampleClassWithNoTypos {
	p := new(ExampleClassWithNoTypos)
	p.name = name
	return p
}

// Adds a [member] to this group.
// param member - member to add
// the new size of the group.
func (r ExampleClassWithNoTypos) goodFunction(member... string) int {
	return 1
}


/**
 * It is <warning descr="ARTICLE_MISSING">friend</warning>
 *
 * <warning descr="PLURAL_VERB_AFTER_THIS">This guy have</warning> no useful logic; it's just a documentation <warning descr="MORFOLOGIK_RULE_EN_US">exmple</warning>.
 *
 */
type ExampleClassWithTypos struct {
	name string
}

// Creates an empty group.
// param name the <warning descr="COMMA_WHICH">name which</warning> group
func NewExampleClassWithTypos(name string) *ExampleClassWithTypos {
	p := new(ExampleClassWithTypos)
	p.name = name
	return p
}

// It <warning descr="IT_VBZ">add</warning> a [member] to this <warning descr="MORFOLOGIK_RULE_EN_US">grooup</warning>.
// param member - member to add
// return the new size of <warning descr="DT_DT">a the</warning> group.
func (r ExampleClassWithTypos) badFunction(member... string) int {
	return 1
}

/**
 * В коробке лежало <warning descr="Sklonenije_NUM_NN">пять карандаша</warning>.
 * А <warning descr="grammar_vse_li_noun">все ли ошибка</warning> найдены?
 * Это случилось <warning descr="INVALID_DATE">31 ноября</warning> 2014 г.
 */
type ForMultiLanguageSupport struct {
	// За весь вечер она <warning descr="ne_proronila_ni">не проронила и слово</warning><warning descr="COMMA_PARENTHESIS_WHITESPACE"> .</warning>
	// Собрание состоится в <warning descr="RU_COMPOUNDS">конференц зале</warning><warning descr="COMMA_PARENTHESIS_WHITESPACE"> .</warning>
	// <warning descr="WORD_REPEAT_RULE">Он он</warning> ошибка.
}
