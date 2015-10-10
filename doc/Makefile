# Makefile for compiling Multimarkdown, and synching document sources to GitLab
# 	* GitLab used: git.ics.ele.tue.nl
#	* Sources: 
#		* Scrivener projects
#		* Related directory structure
#		* Images from Visio
#
# DIRECTIVES:
#	* scrivproj <name>: 
#					- clone a project folder structure as subfolder <name> from GitLab with default files (like this one)
#					- create a Scrivener project <name> 
#					- create a Papers Collection <name>
#					- create a repository <name> at GitLab
#					- open Scrivener to edit the default README.md
#	* git:			create a repository from this project at GitLab;
#	* pdf <name>:	compile a pdf from the default mmd in this project;
#	* branch:		branche the current project into its repository;
#	* release:		merge, compile, commit and branch
#	* clean:		remove all unnecessary files in this project
#	* bib:			create a bib file from Papers


#------------------------------------------------------------------------------#
# Declare variables.                                                           #
#------------------------------------------------------------------------------#

# Leave PROJECT empty to signal command line error
PROJECT = 
GITSRC	= https://git.ics.ele.tue.nl/pbrandt/
TMPLT	= template.scrivener-and-git-project.git 

MMD		= pandoc
LATEX	= pdflatex

MMDS	= $(wildcard *.mmd)
PDFS	= $(MMDS:%.mmd=%.pdf)


#------------------------------------------------------------------------------#
# Make the high level targets.                                                 #
#------------------------------------------------------------------------------#

.PHONY: all clean clone commit

# all: $(PDFS)


#------------------------------------------------------------------------------#
# Make clone from gitlab.														#
# 	Veel te ingewikkeld, want eenvoudiger om                                   #
#		1) handmatig een --bare kloon van t template te maken op github met    #
#			dezelfde naam als gewenste PROJECT.                                #
#		2) handmatig een shellmacro aan te roepen op de juiste plek die:       #
#			- lokaal een dir te maken;                                         #
#			- een lokale git clone maakt van --bare PROJECT;                   #
#			- de juiste Scrivener naamsveranderingen aanbrengt;                #
#			- alle lokaal commit, en terug pushed naar repository.             #
#------------------------------------------------------------------------------#

clone:
	curdir = cd
	@echo === cloning $(GITSRC)$(TMPLT) into $(curdir)/$(PROJECT)
ifndef PROJECT
	$(error clone: No target PROJECT dir specified - use: 'make clone PROJECT=yourChoice')
endif
	git clone $(GITSRC)$(PROJECT).git 
	mv $(addprefix $(PROJECT)/,starter.scriv $(PROJECT).scriv)
	mv $(addprefix $(PROJECT)/$(PROJECT).scriv/,starter.scrivx $(PROJECT).scrivx)
	git add -A
	git commit -m "Created the Scrivener git repository as '$(PROJECT)'. First init, first commit"
	@echo === Git cloned locally! 
	git push
	@echo === Drag and drop the newly cloned folder from the Windows UI over, on to the GitHub for Windows client in order to inform the latter.

	
#------------------------------------------------------------------------------#
# Make the high level targets:                                                 #
#	DAT WERKT DUS NIET                                                         #
#	Problemen te over, waaronder:                                              #
#	- De target wordt wel herkend, maar de prerequisites niet.                 #
#	- Impliciete variabelen worden niet goed resolved.                         #
#------------------------------------------------------------------------------#
	
%.tex: .%.mmd
	$(MMD) -t latex -o $@ -- $<

.%.mmd: src/%.mmd src/mmd/preamble.mmd
	if [[ -f "src/mmd/user.mmd" ]]; then \
		cat -- "src/mmd/preamble.mmd" "src/mmd/user.mmd" $< > $@; \
	else \
		cat -- "src/mmd/preamble.mmd" $< > $@; \
	fi

%.pdf: %.tex
	@echo === running $(LATEX)
	TEXMFHOME=".$${TEXMFHOME:+":$${TEXMFHOME}"}" $(LATEX) -- $<

#------------------------------------------------------------------------------#
# Clean the distribution.                                                      #
#------------------------------------------------------------------------------#

clean:
	-rm -rf -- $(MMDS) $(PDFS) \
		*.aux *.bbl *.blg *.glo *.idx *.ist *.log *.out *.toc
	@echo done cleaning

#------------------------------------------------------------------------------#
# Make the low-level targets.                                                      #
#------------------------------------------------------------------------------#

commit: ;