// -*- mode: C++; c-indent-level: 4; c-basic-offset: 4; tab-width: 8 -*-
/* :tabSize=4:indentSize=4:noTabs=false:folding=explicit:collapseFolds=1: */
//
// comparator_type.h: Rcpp R/C++ interface class library -- comparator
//
// Copyright (C) 2012 Dirk Eddelbuettel and Romain Francois
//
// This file is part of Rcpp.
//
// Rcpp is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.
//
// Rcpp is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Rcpp.  If not, see <http://www.gnu.org/licenses/>.

#ifndef Rcpp__traits__comparator_type__h
#define Rcpp__traits__comparator_type__h

namespace Rcpp{
namespace traits{
   
class StringCompare {
public:
    inline bool operator()( SEXP x, SEXP y) const {
        return strcmp( char_nocheck(x), char_nocheck(y) ) < 0 ; 
    }
} ;
	
template <int RTYPE> struct comparator_type {
	typedef std::less< typename storage_type<RTYPE>::type > type ;	
} ;
template <> struct comparator_type<STRSXP>{
	typedef StringCompare type ;
} ;
   

}
}     

#endif

